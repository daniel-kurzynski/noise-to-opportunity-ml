/**********************************************************************************************
 * Copyright 2009 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file 
 * except in compliance with the License. A copy of the License is located at
 *
 *       http://aws.amazon.com/apache2.0/
 *
 * or in the "LICENSE.txt" file accompanying this file. This file is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License. 
 *
 * ********************************************************************************************
 *
 *  Amazon Product Advertising API
 *  Signed Requests Sample Code
 *
 *  API Version: 2009-03-31
 *
 */

package com.amazon.advertising.api.sample;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*
 * This class shows how to make a simple authenticated ItemLookup call to the
 * Amazon Product Advertising API.
 * 
 * See the README.html that came with this sample for instructions on
 * configuring and running the sample.
 */
public class ItemLookupSample {

    public static enum OPERATIONS {
        ItemSearch,
        ItemLookup
    }

    /*
     * Your AWS Access Key ID, as taken from the AWS Your Account page.
     */
    private static final String AWS_ACCESS_KEY_ID = "AKIAIXIO43RVEO33FP7A";

    /*
     * Your AWS Secret Key corresponding to the above ID, as taken from the AWS
     * Your Account page.
     */
    private static final String AWS_SECRET_KEY = "eeV6V0W5Vt0rNNeq7TwSkLhNTEMD/5k89d5bM6Dp";

    /*
     * Use one of the following end-points, according to the region you are
     * interested in:
     * 
     *      US: ecs.amazonaws.com 
     *      CA: ecs.amazonaws.ca 
     *      UK: ecs.amazonaws.co.uk 
     *      DE: ecs.amazonaws.de 
     *      FR: ecs.amazonaws.fr 
     *      JP: ecs.amazonaws.jp
     * 
     */
    private static final String ENDPOINT = "ecs.amazonaws.de";

    /*
     * The Item ID to lookup. The value below was selected for the US locale.
     * You can choose a different value if this value does not work in the
     * locale of your choice.
     */
    private static final String ITEM_ID = "0545010225";

    public static void main(String[] args) {
        /*
         * Set up the signed requests helper 
         */
        com.amazon.advertising.api.sample.SignedRequestsHelper helper;
        try {
            helper = SignedRequestsHelper.getInstance(ENDPOINT, AWS_ACCESS_KEY_ID, AWS_SECRET_KEY);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        
        String requestUrl = null;
        String title = null;

        /* The helper can sign requests in two forms - map form and string form */
        
        /*
         * Here is an example in map form, where the request parameters are stored in a map.
         */
        Map<String, String> params = new HashMap<String, String>(){{
            put("Service", "AWSECommerceService");
            put("AssociateTag", "PutYourAssociateTagHere");
            put("Version", "2011-08-01");
            put("Operation", OPERATIONS.ItemSearch.toString());
//            put("ItemId", ITEM_ID);
            put("Keywords", "Java Books");
            put("SearchIndex", "Books");
            put("ResponseGroup", "Small");
        }};

        requestUrl = helper.sign(params);
        System.out.println(requestUrl);
        try {
            System.out.println("==== REVIEWS: ====");
            for(String url: getReviewsUrls(requestUrl)){
                System.out.println(url);
            }
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

//        title = fetchTitle(requestUrl);
//        System.out.println("Signed Title is \"" + title + "\"");
//        System.out.println();

//        /* Here is an example with string form, where the requests parameters have already been concatenated
//         * into a query string. */
//        System.out.println("String form example:");
//        String queryString = "Service=AWSECommerceService&AssociateTag=PutYourAssociateTagHere&Version=2009-03-31&Operation=ItemLookup&ResponseGroup=Small&ItemId="
//                + ITEM_ID;
//        requestUrl = helper.sign(queryString);
//        System.out.println("Request is \"" + requestUrl + "\"");
//
//        title = fetchTitle(requestUrl);
//        System.out.println("Title is \"" + title + "\"");
//        System.out.println();

    }

    private static List<String> getReviewsUrls(String requestUrl) throws ParserConfigurationException, SAXException, IOException{
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(requestUrl);
//        System.out.println(doc.getDocumentURI());
        NodeList urlNodes = doc.getElementsByTagName("URL");
        List<String> urls = new ArrayList<>();
        for (int i = 0; i < urlNodes.getLength(); i++){
            String url = urlNodes.item(i).getTextContent();
            if(url.contains("review")){
                urls.add(url);
            }
        }
        return urls;
    }

    /*
     * Utility function to fetch the response from the service and extract the
     * title from the XML.
     */
    private static String fetchTitle(String requestUrl) {
        String title = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(requestUrl);
            System.out.println(doc.getDocumentURI());
            Node titleNode = doc.getElementsByTagName("Title").item(0);

            if(titleNode == null){
                return "NO TITLE";
            }
            title = titleNode.getTextContent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return title;
    }

}
