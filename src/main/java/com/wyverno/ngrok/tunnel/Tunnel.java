package com.wyverno.ngrok.tunnel;

public class Tunnel {
    private Endpoint endpoint;

    private String forwards_to;

    private String id;

    private String  proto;

    private String public_url;

    private String region;

    private String started_at;

    private TunnelSession tunnel_session;

    public Endpoint getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Endpoint endpoint) {
        this.endpoint = endpoint;
    }

    public String getForwards_to() {
        return forwards_to;
    }

    public void setForwards_to(String forwards_to) {
        this.forwards_to = forwards_to;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProto() {
        return proto;
    }

    public void setProto(String proto) {
        this.proto = proto;
    }

    public String getPublic_url() {
        return public_url;
    }

    public void setPublic_url(String public_url) {
        this.public_url = public_url;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getStarted_at() {
        return started_at;
    }

    public void setStarted_at(String started_at) {
        this.started_at = started_at;
    }

    public TunnelSession getTunnel_session() {
        return tunnel_session;
    }

    public void setTunnel_session(TunnelSession tunnel_session) {
        this.tunnel_session = tunnel_session;
    }
}
