package org.apache.jmeter.visualizers.backend.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.jmeter.visualizers.SamplingStatCalculator;
import org.apache.jmeter.visualizers.StatVisualizer;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class HttpServer extends Thread {

	private InputStream input;
	
	private OutputStream out;
 
	public HttpServer(Socket socket) {
		try {
			input = socket.getInputStream();
			out = socket.getOutputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
 
	/**
	 * 多线程方法调用
	 */
	@Override
	public void run() {
		response(read());
	}
	
	private void response(String key) {
		StringBuffer sb = new StringBuffer();
		if (Objects.isNull(key) || key.isEmpty() || "/".equals(key)) {
			sb.append(JSONObject.toJSONString(toAllMap(RestBackendListener.tableRows)));
		} else {
			try {
				String str2 = URLDecoder.decode(key.substring(1), "UTF-8");
				sb.append(JSONObject.toJSONString(toMap(RestBackendListener.tableRows.get(str2))));
			} catch (UnsupportedEncodingException e) {
				sb.append(JSONObject.toJSONString(toAllMap(RestBackendListener.tableRows)));
			}
		}
		try {
			StringBuffer result = new StringBuffer();
			result.append("HTTP/1.1 200 ok \r\n");
            result.append("Content-Language:zh-CN \r\n");
            // charset=UTF-8 解决中文乱码问题
            result.append("Content-Type:text/html;charset=UTF-8 \r\n");
//            result.append("Content-Length:" + sb.length() + "\r\n");
            result.append("\r\n" + sb.toString());
            out.write(result.toString().getBytes());
            out.flush();
            out.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
	
	private String read() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        try {
            // 读取请求头， 如：GET /index.html HTTP/1.1
            String readLine = reader.readLine();
            String[] split = readLine.split(" ");
            if (split.length != 3) {
                return null;
            }
//            System.out.println(readLine);
            return split[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
	
	private static Map<String, Object> toMap(SamplingStatCalculator ssc) {
		Map<String, Object> map = new HashMap<>();
		if(Objects.nonNull(ssc)) {
			map.put("max", ssc.getMax());
			map.put("min", ssc.getMin());
			map.put("avg", ssc.getAvgPageBytes());
			map.put("label", ssc.getLabel());
			map.put("throughput", ssc.getMaxThroughput());
			map.put("received", ssc.getKBPerSecond());
		}
		return map;
	}
	
	private static Map<String, Object> toAllMap(Map<String, SamplingStatCalculator> map) {
		Map<String, Object> rtnMap = new HashMap<>();
		if(Objects.nonNull(map) || !map.isEmpty()) {
			map.forEach((k, v) -> {
				rtnMap.put(k, toMap((SamplingStatCalculator)v)); 
			});	
		}
		return rtnMap;
	}
 
}