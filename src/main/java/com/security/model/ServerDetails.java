package com.security.model;

import lombok.Data;

//@Data
public class ServerDetails {
        private long serverId;
        private String serverName;
        private String ipAddress;
        private int portno;
        private String loginName;
        private String password;
        private int connectiontype;
        private int isdelete;
        private int active;

        public ServerDetails(long serverId, String serverName, String ipAddress, int portno, String loginName, String password, int connectiontype) {
                this.serverId = serverId;
                this.serverName = serverName;
                this.ipAddress = ipAddress;
                this.portno = portno;
                this.loginName = loginName;
                this.password = password;
                this.connectiontype = connectiontype;
                this.isdelete = 0;
                this.active = 1;
        }

        public long getServerId() {
                return serverId;
        }

        public void setServerId(long serverId) {
                this.serverId = serverId;
        }

        public String getServerName() {
                return serverName;
        }

        public void setServerName(String serverName) {
                this.serverName = serverName;
        }

        public String getIpAddress() {
                return ipAddress;
        }

        public void setIpAddress(String ipAddress) {
                this.ipAddress = ipAddress;
        }

        public int getPortno() {
                return portno;
        }

        public void setPortno(int portno) {
                this.portno = portno;
        }

        public String getLoginName() {
                return loginName;
        }

        public void setLoginName(String loginName) {
                this.loginName = loginName;
        }

        public String getPassword() {
                return password;
        }

        public void setPassword(String password) {
                this.password = password;
        }

        public int getConnectiontype() {
                return connectiontype;
        }

        public void setConnectiontype(int connectiontype) {
                this.connectiontype = connectiontype;
        }

        public int getIsdelete() {
                return isdelete;
        }

        public void setIsdelete(int isdelete) {
                this.isdelete = isdelete;
        }

        public int getActive() {
                return active;
        }

        public void setActive(int active) {
                this.active = active;
        }
}
