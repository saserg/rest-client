package com.github.saserg.restclient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "clientId",
        "isActive",
        "nickName"
})

public class UserData implements Serializable {

    @JsonProperty("clientId")
    private Integer clientId;
    @JsonProperty("isActive")
    private Boolean isActive;
    @JsonProperty("nickName")
    private String nickName;

    public Integer getClientId() {
        return clientId;
    }

    public Boolean getActive() {
        return isActive;
    }

    public String getNickName() {
        return nickName;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append("clientId");
        sb.append('=');
        sb.append(((this.clientId == null)?"<null>":this.clientId));
        sb.append(',');
        sb.append("isActive");
        sb.append('=');
        sb.append(((this.isActive == null)?"<null>":this.isActive));
        sb.append(',');
        sb.append("nickName");
        sb.append('=');
        sb.append(((this.nickName == null)?"<null>":this.nickName));
        sb.append(',');
        if (sb.charAt((sb.length()- 1)) == ',') {
            sb.setCharAt((sb.length()- 1), ']');
        } else {
            sb.append(']');
        }
        return sb.toString();
    }
}