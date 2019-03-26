package org.apache.jmeter.visualizers.backend.rest;

import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.threads.JMeterContextService;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.SamplingStatCalculator;
import org.apache.jmeter.visualizers.backend.AbstractBackendListenerClient;
import org.apache.jmeter.visualizers.backend.BackendListenerContext;

public class RestBackendListener extends AbstractBackendListenerClient{
	
	public static final Map<String, SamplingStatCalculator> tableRows = new ConcurrentHashMap<>();

    private Deque<SamplingStatCalculator> newRows = new ConcurrentLinkedDeque<>();
   

	@Override
	public void handleSampleResults(List<SampleResult> sampleResults, BackendListenerContext context) {
		sampleResults.stream().forEach(sampleResult -> {

//			threadGroupName =  JMeterContextService.getContext().getThreadGroup().getName();
	        SamplingStatCalculator row = tableRows.computeIfAbsent(sampleResult.getSampleLabel(true), label -> {
	           SamplingStatCalculator newRow = new SamplingStatCalculator(label);
	           newRows.add(newRow);
	           return newRow;
	        });
	        synchronized(row) {
	            /*
	             * Synch is needed because multiple threads can update the counts.
	             */
	            row.addSample(sampleResult);
//	            System.out.println(row.toString());
	        }		
		});
	}

}
