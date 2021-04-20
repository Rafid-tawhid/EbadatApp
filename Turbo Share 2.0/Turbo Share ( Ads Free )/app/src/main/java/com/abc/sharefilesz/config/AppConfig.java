

package com.abc.sharefilesz.config;

public class AppConfig
{
    public final static int
            SERVER_PORT_COMMUNICATION = 1128,
            SERVER_PORT_WEBSHARE = 58732,
            SERVER_PORT_UPDATE_CHANNEL = 58765,
            DEFAULT_SOCKET_TIMEOUT = 5000, 
            DEFAULT_SOCKET_TIMEOUT_LARGE = 20000, 
            DEFAULT_NOTIFICATION_DELAY = 2000, 
            SUPPORTED_MIN_VERSION = 99, 
            NICKNAME_LENGTH_MAX = 32,
            BUFFER_LENGTH_DEFAULT = 8096,
            DELAY_CHECK_FOR_UPDATES = 21600,
            PHOTO_SCALE_FACTOR = 100,
            WEB_SHARE_CONNECTION_MAX = 20,
            ID_GROUP_WEB_SHARE = 10;

    public final static String
            PREFIX_ACCESS_POINT = "TS_",
            EXT_FILE_PART = "tshare",
            NDS_COMM_SERVICE_NAME = "TSComm",
            NDS_COMM_SERVICE_TYPE = "_tscomm._tcp.";


    public final static String[] DEFAULT_DISABLED_INTERFACES = new String[]{"rmnet"};

}
