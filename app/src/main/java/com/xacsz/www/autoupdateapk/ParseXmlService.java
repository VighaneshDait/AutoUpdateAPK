package com.xacsz.www.autoupdateapk;

/**
 * Created by Administrator on 7/5/2016.
 */

import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


public class ParseXmlService
{
    public HashMap<String, String> parseXml(InputStream inStream) throws Exception
    {
        HashMap<String, String> hashMap = new HashMap<String, String>();


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = factory.newDocumentBuilder();

        Document document = builder.parse(inStream);

        Element root = document.getDocumentElement();

        NodeList childNodes = root.getChildNodes();
        for (int j = 0; j < childNodes.getLength(); j++)
        {
            //遍历子节点
            Node childNode = (Node) childNodes.item(j);
            if (childNode.getNodeType() == Node.ELEMENT_NODE)
            {
                Element childElement = (Element) childNode;
                //版本号
                if ("version".equals(childElement.getNodeName()))
                {
                    hashMap.put("version",childElement.getFirstChild().getNodeValue());
                }
                //软件名称
                else if (("name".equals(childElement.getNodeName())))
                {
                    hashMap.put("name",childElement.getFirstChild().getNodeValue());
                }
                //下载地址
                else if (("url".equals(childElement.getNodeName())))
                {
                    hashMap.put("url",childElement.getFirstChild().getNodeValue());
                }
            }
        }
        return hashMap;
    }
}
