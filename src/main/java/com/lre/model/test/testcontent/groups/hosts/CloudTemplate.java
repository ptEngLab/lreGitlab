package com.lre.model.test.testcontent.groups.hosts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.lre.actions.utils.ConfigConstants.LRE_API_XMLNS;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "CloudTemplate", namespace = LRE_API_XMLNS)
public class CloudTemplate {


    @JsonProperty("Id")
    @JacksonXmlProperty(localName = "Id", namespace = LRE_API_XMLNS)
    private Integer id;

    @JsonProperty("AccountId")
    @JacksonXmlProperty(localName = "AccountId", namespace = LRE_API_XMLNS)
    private Integer accountId;

    @JsonProperty("AccountName")
    @JacksonXmlProperty(localName = "AccountName", namespace = LRE_API_XMLNS)
    private String accountName;

    @JsonProperty("Name")
    @JacksonXmlProperty(localName = "Name", namespace = LRE_API_XMLNS)
    private String name;

    @JsonProperty("Image")
    @JacksonXmlProperty(localName = "Image", namespace = LRE_API_XMLNS)
    private String image;

    @JsonProperty("InstanceType")
    @JacksonXmlProperty(localName = "InstanceType", namespace = LRE_API_XMLNS)
    private String instanceType;

    @JsonProperty("KeyPairName")
    @JacksonXmlProperty(localName = "KeyPairName", namespace = LRE_API_XMLNS)
    private String keyPairName;

    @JsonProperty("SecurityGroupName")
    @JacksonXmlProperty(localName = "SecurityGroupName", namespace = LRE_API_XMLNS)
    private String securityGroupName;

    @JsonProperty("Description")
    @JacksonXmlProperty(localName = "Description", namespace = LRE_API_XMLNS)
    private String description;

    @JsonProperty("Installation")
    @JacksonXmlProperty(localName = "Installation", namespace = LRE_API_XMLNS)
    private String installation;

    @JsonProperty("VpcSubnetId")
    @JacksonXmlProperty(localName = "VpcSubnetId", namespace = LRE_API_XMLNS)
    private String vpcSubnetId;

    @JsonProperty("Platform")
    @JacksonXmlProperty(localName = "Platform", namespace = LRE_API_XMLNS)
    private String platform;

    @JsonProperty("IsValid")
    @JacksonXmlProperty(localName = "IsValid", namespace = LRE_API_XMLNS)
    private Boolean isValid;

    @JsonProperty("NetworkName")
    @JacksonXmlProperty(localName = "NetworkName", namespace = LRE_API_XMLNS)
    private String networkName;

    @JsonProperty("Region")
    @JacksonXmlProperty(localName = "Region", namespace = LRE_API_XMLNS)
    private String region;

    @JsonProperty("UseElasticIp")
    @JacksonXmlProperty(localName = "UseElasticIp", namespace = LRE_API_XMLNS)
    private Boolean useElasticIp;

    @JsonProperty("UsePrivateIp")
    @JacksonXmlProperty(localName = "UsePrivateIp", namespace = LRE_API_XMLNS)
    private Boolean usePrivateIp;
}
