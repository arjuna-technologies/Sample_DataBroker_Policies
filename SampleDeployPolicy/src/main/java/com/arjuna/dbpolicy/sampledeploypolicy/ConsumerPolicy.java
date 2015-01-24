/*
 * Copyright (c) 2014-2015, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved. 
 */

package com.arjuna.dbpolicy.sampledeploypolicy;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import com.arjuna.agility.ServiceAgreement;
import com.arjuna.agility.ServiceAgreementContext;
import com.arjuna.agility.ServiceAgreementListener;
import com.arjuna.agility.ServiceAgreementListenerException;
import com.arjuna.agility.Vote;
import com.arjuna.agility.view.Relationship;
import com.arjuna.databroker.data.DataFlow;
import com.arjuna.databroker.data.DataFlowFactory;
import com.arjuna.databroker.data.DataFlowInventory;
import com.arjuna.databroker.data.DataFlowNodeFactory;
import com.arjuna.databroker.data.DataFlowNodeFactoryInventory;
import com.arjuna.databroker.data.DataProcessor;
import com.arjuna.databroker.data.DataService;
import com.arjuna.databroker.data.DataSource;
import com.arjuna.databroker.data.connector.ObservableDataProvider;
import com.arjuna.databroker.data.connector.ObserverDataConsumer;
import com.arjuna.dbpolicy.sampledeploypolicy.view.SampleDeployView;

@Stateless
public class ConsumerPolicy implements ServiceAgreementListener
{
    private static final Logger logger = Logger.getLogger(ConsumerPolicy.class.getName());

    public void onRegistered(String domain)
        throws ServiceAgreementListenerException
    {
    }

    public Vote onChangeProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        if (serviceAgreement.isCompatible(Relationship.class))
        {
            if (! serviceAgreementContext.isLocal())
                return Vote.reject("Invalid Relationship", "Remote creation/modification of Relationships not supported");
            else
                return Vote.accept();
        }
        else if (serviceAgreement.isCompatible(SampleDeployView.class))
        {
            if (! serviceAgreementContext.isLocal())
                return Vote.reject("Invalid SLA", "Remote creation/modification of SLAs not supported");
            else
                return Vote.accept();
        }
        else
            return Vote.ignore();
    }

    public void onChanged(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        if (serviceAgreement.isCompatible(SampleDeployView.class))
        {
            SampleDeployView sampleDeployView = serviceAgreement.asView(SampleDeployView.class);

            System.err.println("onChanged: FlowName:      " + sampleDeployView.getFlowName());
            System.err.println("onChanged: SourceName:    " + sampleDeployView.getSourceName());
            System.err.println("onChanged: ProcessorName: " + sampleDeployView.getProcessorName());
            System.err.println("onChanged: ServiceName:   " + sampleDeployView.getServiceName());
            
            createDataFlow(sampleDeployView.getFlowName(), sampleDeployView.getSourceName(), sampleDeployView.getProcessorName(), sampleDeployView.getServiceName());
        }
    }

    public void onChangeRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        if (serviceAgreement.isCompatible(SampleDeployView.class))
        {
            SampleDeployView sampleDeployView = serviceAgreement.asView(SampleDeployView.class);

            System.err.println("onChangeRejected: FlowName:      " + sampleDeployView.getFlowName());
            System.err.println("onChangeRejected: SourceName:    " + sampleDeployView.getSourceName());
            System.err.println("onChangeRejected: ProcessorName: " + sampleDeployView.getProcessorName());
            System.err.println("onChangeRejected: ServiceName:   " + sampleDeployView.getServiceName());
        }
    }

    public Vote onTerminateProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        if (serviceAgreement.isCompatible(Relationship.class))
            return Vote.accept();
        else if (serviceAgreement.isCompatible(SampleDeployView.class))
            return Vote.accept();
        else
            return Vote.ignore();
    }

    public void onTerminated(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
    }

    public void onTerminateRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
    }

    public void onUnregistered(String domain)
         throws ServiceAgreementListenerException
    {
    }

    private void createDataFlow(String flowName, String sourceName, String processorName, String serviceName)
    {
        try
        {
            Map<String, String> metaProperties = new HashMap<String, String>();
            Map<String, String> properties     = new HashMap<String, String>();
            metaProperties.put("Type", "Standard");

            DataFlow dataFlow = _dataFlowFactory.createDataFlow(flowName, metaProperties, properties);
            for (DataFlowNodeFactory dataFlowNodeFactory: _dataFlowNodeFactoryInventory.getDataFlowNodeFactorys())
            {
                System.err.println("createDataFlow - dataFlowNodeFactory: " + dataFlowNodeFactory.getName());
                dataFlow.getDataFlowNodeFactoryInventory().addDataFlowNodeFactory(dataFlowNodeFactory);
            }
            _dataFlowInventory.addDataFlow(dataFlow);

            DataFlowNodeFactory dataFlowNodeFactory = dataFlow.getDataFlowNodeFactoryInventory().getDataFlowNodeFactory("Simple Data Source Factory");
            if (dataFlowNodeFactory != null)
            {
                DataSource    dataSource    = dataFlowNodeFactory.createDataFlowNode(sourceName, DataSource.class, Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap());
                DataProcessor dataProcessor = dataFlowNodeFactory.createDataFlowNode(processorName, DataProcessor.class, Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap());
                DataService   dataService   = dataFlowNodeFactory.createDataFlowNode(serviceName, DataService.class, Collections.<String, String>emptyMap(), Collections.<String, String>emptyMap());

                _dataFlowNodeLifeCycleControl.processCreatedDataFlowNode(dataSource, dataFlow);
                _dataFlowNodeLifeCycleControl.processCreatedDataFlowNode(dataProcessor, dataFlow);
                _dataFlowNodeLifeCycleControl.processCreatedDataFlowNode(dataService, dataFlow);

                ((ObservableDataProvider<String>) dataSource.getDataProvider(String.class)).addDataConsumer((ObserverDataConsumer<String>) dataProcessor.getDataConsumer(String.class));
                ((ObservableDataProvider<String>) dataProcessor.getDataProvider(String.class)).addDataConsumer((ObserverDataConsumer<String>) dataService.getDataConsumer(String.class));
            }
            else
                logger.log(Level.WARNING, "Unable to find DataFlowNode Factory 'Sample Data Source Factory'");
        }
        catch (Throwable throwable)
        {
            logger.log(Level.WARNING, "Problem when creating DataFlow", throwable);
        }
    }

    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowFactory")
    private DataFlowFactory _dataFlowFactory;
    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowInventory")
    private DataFlowInventory _dataFlowInventory;
    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowNodeFactoryInventory")
    private DataFlowNodeFactoryInventory _dataFlowNodeFactoryInventory;
    @EJB(lookup="java:global/databroker/data-core-jee/DataFlowNodeLifeCycleControl")
    private DataFlowNodeLifeCycleControl _dataFlowNodeLifeCycleControl;
}
