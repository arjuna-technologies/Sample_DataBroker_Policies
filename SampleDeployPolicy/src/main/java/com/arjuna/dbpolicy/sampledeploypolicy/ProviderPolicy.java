/*
 * Copyright (c) 2014, Arjuna Technologies Limited, Newcastle-upon-Tyne, England. All rights reserved. 
 */

package com.arjuna.dbpolicy.sampledeploypolicy;

import javax.ejb.Singleton;
import com.arjuna.agility.ServiceAgreement;
import com.arjuna.agility.ServiceAgreementContext;
import com.arjuna.agility.ServiceAgreementListener;
import com.arjuna.agility.ServiceAgreementListenerException;
import com.arjuna.agility.Vote;
import com.arjuna.agility.view.Relationship;
import com.arjuna.dbpolicy.sampledeploypolicy.view.SampleDeployView;

@Singleton
public class ProviderPolicy implements ServiceAgreementListener
{
    public void onRegistered(String domain)
        throws ServiceAgreementListenerException
    {
    }

    public Vote onChangeProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
         throws ServiceAgreementListenerException
    {
        if (serviceAgreement.isCompatible(Relationship.class))
            return Vote.accept();
        else if (serviceAgreement.isCompatible(SampleDeployView.class))
        {
            SampleDeployView sampleDeployView = serviceAgreement.asView(SampleDeployView.class);

            if ((sampleDeployView.getFlowName() == null) || (sampleDeployView.getSourceName() == null) || (sampleDeployView.getProcessorName() == null) || (sampleDeployView.getServiceName() == null))
                return Vote.reject("Invalid SLA", "Not all names present");
            else
                return Vote.accept();
        }
        else
            return Vote.reject();
    }

    public void onChanged(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
    }

    public void onChangeRejected(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
    }

    public Vote onTerminateProposed(ServiceAgreement serviceAgreement, ServiceAgreementContext serviceAgreementContext)
        throws ServiceAgreementListenerException
    {
        if (serviceAgreement.isCompatible(Relationship.class))
            return Vote.accept();
        else if (serviceAgreement.isCompatible(SampleDeployView.class))
        {
            SampleDeployView sampleDeployView = serviceAgreement.asView(SampleDeployView.class);

            if ((sampleDeployView.getFlowName() == null) || (sampleDeployView.getSourceName() == null) || (sampleDeployView.getProcessorName() == null) || (sampleDeployView.getServiceName() == null))
                return Vote.reject("Invalid SLA", "Not all names present");
            else
                return Vote.accept();
        }
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
}