package org.cloudbus.cloudsim.power;

import java.util.List;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.util.MathUtil;


public class KMeansMadIQRVM extends PowerVmAllocationPolicyMigrationAbstract{
	
	private double safetyparameter = 0;
	
	private PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy;
	
	public KMeansMadIQRVM(
			List<?extends Host> hostList,
			PowerVmSelectionPolicy VmSelectionPolicy,
			double safetyParameter,
			PowerVmAllocationPolicyMigrationAbstract fallbackVmAllocationPolicy
			) {
		super(hostList, vmSelectionPolicy);
		setSafetyParameter(safetyParameter);
		setFallbackVmAllocationPolicy(fallbackVmAllocationPolicy);
		
	}
	
	
	@Override
	protected boolean isHostOverUtilized(PowerHost host) {
		PowerHostUtilizationHistory _host = (PowerHostUtilizationHistory) host;
		double upperThresold = 0;
		try {
			upperThresold = 1-getSafetyParameter()*getHostUtilizationTqr(_host);
		}
		catch(IllegalArgumentException e) {
			return getFallbackVmAllocationPolicy().isHostOverUtilized(host);
		}
		addHistoryEntry(host, upperThresold);
		double totalRequestedMips = 0;
		for(Vm vm :host.getVmList()) {
			totalRequestedMips += vm.getCurrentRequestedTotalMips(); 
		}
		double utilization = totalRequestedMips/host.getTotalMips();
		return utilization > upperThresold;
	}
	
	protected boolean isHostLowUtilized(PowerHost host) {
		PowerHostUtilizationHistory _host = (PowerHostUtilizationHistory) host;
		double upperThresold = 0;
		double upperThresoldA = 0;
		try {
			upperThresoldA = 1-getSafetyParameter()*getHostUtilizationTqr(_host);
			upperThresold = 0.5*upperThresoldA;
		}
		catch(IllegalArgumentException e) {
			return getFallbackVmAllocationPolicy().isHostLowUtilized(host);
		}
		//addHistoryEntry(host, upperThresold);
		double totalRequestedMips = 0;
		for(Vm vm :host.getVmList()) {
			totalRequestedMips += vm.getCurrentRequestedTotalMips(); 
		}
		double utilization = totalRequestedMips/host.getTotalMips();
		return utilization < upperThresold;
	}
	
	// Middle
	//Medium 
	//Light
	
	
	protected double getHostUtilizationTqr(PowerHostUtilizationHistory host) throws IllegalArgumentException{
		double[] p = host.getUtilizationHistory();
		if(MathUtil.countNonZeroBeginning(p)>=12) {
			int k = 5;
			double data[] = new double[k];
			double[][] g;
			g = cluster(p,k);
			double Midrange[] = new double[k];
			for(int i=0; i<g.length;i++) {
				double Max = MathUtil.Max(g[i]);
				double Min = MathUtil.Min((g[i]));
				Midrange[i] = (Max+Min)/2;	
			}
			return MathUtil.iqr(Midrange);
		}
	}
	
	
}