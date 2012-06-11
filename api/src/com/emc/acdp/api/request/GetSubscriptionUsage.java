// Copyright (c) 2012, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification, 
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice, 
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright 
//       notice, this list of conditions and the following disclaimer in the 
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote 
//       products derived from this software without specific prior written 
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//      POSSIBILITY OF SUCH DAMAGE.
package com.emc.acdp.api.request;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import com.emc.cdp.services.rest.model.MeteringUsageList;

/**
 * @author cwikj
 * 
 */
public class GetSubscriptionUsage extends AcdpXmlResponseRequest<MeteringUsageList> {
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    
    private Date startDate;
    private Date endDate;
    private String accountId;
    private String subscriptionId;
    private List<String> resources;
    private String category;
    private int start = -1;
    private int count = -1;
    private String adminSessionId;

    /**
     * Cretes a new subscription usage request.
     * 
     * @param accountId
     *            the Account ID.
     * @param subscriptionId
     *            the subscription ID to query in the account
     * @param resources
     *            the resources to query. Supported resources include:
     *            DiskUsage, UmdDiskUsage, BandwidthIn, BandwidthOut,
     *            TransactionNum. Resources can also include a policy specifier,
     *            e.g. DiskUsage/policy or DiskUsage/* for all policies.
     * @param category
     *            Supported categories include: total, daily, hourly,
     *            bytokengroup, bytoken, byuser.
     */
    public GetSubscriptionUsage(String accountId, String subscriptionId,
            Date startDate, Date endDate,
            List<String> resources, String category, String adminSessionId) {
        this.accountId = accountId;
        this.subscriptionId = subscriptionId;
        this.resources = resources;
        this.category = category;
        this.startDate = startDate;
        this.endDate = endDate;
        this.adminSessionId = adminSessionId;
    }

    @Override
    public String getRequestPath() {
        return MessageFormat.format(
                "/cdp-rest/v1/admin/accounts/{0}/storage/{1}/usage",
                accountId, subscriptionId);
    }

    @Override
    public String getRequestQuery() {
        DateFormat df = new SimpleDateFormat(DATE_FORMAT);
        
        StringBuffer resBuf = new StringBuffer();
        for(int i=0; i<resources.size(); i++) {
            if(i>0) {
                resBuf.append(",");
            }
            resBuf.append(resources.get(i));
        }
        
        String q = MessageFormat.format("start_date={0}&end_date={1}&resources={2}&cat={3}", df.format(startDate), df.format(endDate), resBuf.toString(), category);
        if(start > -1) {
            q += "&start=" + start;
        }
        
        if(count > -1) {
            q += "&count=" + count;
        }
        
        q+= "&" + CDP_SESSION_PARAM + "=" + adminSessionId;
        
        return q;
    }

    @Override
    public String getMethod() {
        return GET_METHOD;
    }

    @Override
    public long getRequestSize() {
        return -1;
    }

    @Override
    public byte[] getRequestData() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @return the accountId
     */
    public String getAccountId() {
        return accountId;
    }

    /**
     * @param accountId
     *            the accountId to set
     */
    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    /**
     * @return the subscriptionId
     */
    public String getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * @param subscriptionId
     *            the subscriptionId to set
     */
    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * @return the resources
     */
    public List<String> getResources() {
        return resources;
    }

    /**
     * @param resources
     *            the resources to set
     */
    public void setResources(List<String> resources) {
        this.resources = resources;
    }

    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category
     *            the category to set Supported categories include: total,
     *            daily, hourly, bytokengroup, bytoken, byuser.
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return the start
     */
    public int getStart() {
        return start;
    }

    /**
     * @param start
     *            the start to set (1 is the first item)
     */
    public void setStart(int start) {
        if(start == 0) {
            throw new IllegalArgumentException("Start must be >0 (or -1 for default)");
        }
        this.start = start;
    }

    /**
     * @return the count
     */
    public int getCount() {
        return count;
    }

    /**
     * @param count
     *            the count to set
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

}
