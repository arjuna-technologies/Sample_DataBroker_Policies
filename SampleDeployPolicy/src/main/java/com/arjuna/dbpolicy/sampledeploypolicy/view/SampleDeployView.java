/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved. 
 */

package com.arjuna.dbpolicy.sampledeploypolicy.view;

import com.arjuna.agility.annotation.Feature;

public interface SampleDeployView
{
    @Feature(name = "agility.sampledeploy.flowname")
    public String getFlowName();

    public void setFlowName(String flowName);

    @Feature(name = "agility.sampledeploy.sourcename")
    public String getSourceName();

    public void setSourceName(String sourceName);

    @Feature(name = "agility.sampledeploy.processorname")
    public String getProcessorName();

    public void setProcessorName(String processorName);

    @Feature(name = "agility.sampledeploy.servicename")
    public String getServiceName();

    public void setServiceName(String serviceName);
}
