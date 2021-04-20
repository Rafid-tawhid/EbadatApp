

package com.abc.sharefilesz.config;

/**

 * Date: 4/28/17 8:29 PM
 */

public class Keyword
{
    public static final String
            REQUEST = "request",
            RESULT = "result",
            REQUEST_TRANSFER = "requestTransfer",
            REQUEST_RESPONSE = "requestResponse",
            REQUEST_ACQUAINTANCE = "requestAcquaintance",
            REQUEST_CLIPBOARD = "requestClipboard",
            REQUEST_HANDSHAKE = "requestHandshake",
            REQUEST_TRANSFER_JOB = "requestTransferJob", // Introduced in 99
            TRANSFER_TYPE = "transferType", // Introduced in 99
            REQUEST_UPDATE = "backCompRequestSendUpdate",
            REQUEST_UPDATE_V2 = "backwardsRequestUpdate", // Introduced in 99
            TRANSFER_REQUEST_ID = "requestId",
            TRANSFER_GROUP_ID = "groupId",
            TRANSFER_DEVICE_ID = "deviceId", // Introduced in 91
            TRANSFER_SOCKET_PORT = "socketPort",
            TRANSFER_IS_ACCEPTED = "isAccepted",
            TRANSFER_CLIPBOARD_TEXT = "clipboardText",
            TRANSFER_JOB_DONE = "jobDone",
            FLAG = "flag",
            FLAG_GROUP_EXISTS = "flagGroupExists",
            FILES_INDEX = "filesIndex",
            INDEX_FILE_NAME = "file",
            INDEX_FILE_SIZE = "fileSize",
            INDEX_FILE_MIME = "fileMime",
            INDEX_DIRECTORY = "directory",
            DEVICE_INFO = "deviceInfo",
            DEVICE_INFO_SERIAL = "deviceId",
            DEVICE_INFO_BRAND = "brand",
            DEVICE_INFO_MODEL = "model",
            DEVICE_INFO_USER = "user",
            DEVICE_INFO_KEY = "key", // Introduced in 99
            DEVICE_INFO_PICTURE = "devicePicture",
            DEVICE_PIN = "pin", // Introduced in 99
            APP_INFO = "appInfo",
            APP_INFO_VERSION_NAME = "versionName",
            APP_INFO_VERSION_CODE = "versionCode",
            APP_INFO_CLIENT_VERSION = "clientVersion", // Introduced in 99
            SKIPPED_BYTES = "skippedBytes",
            SIZE_CHANGED = "sizeChanged",
            ERROR = "error",
            ERROR_NOT_ALLOWED = "notAllowed",
            ERROR_NOT_FOUND = "notFound",
            ERROR_UNKNOWN = "errorUnknown",
            ERROR_NOT_ACCESSIBLE = "notAccessible",
            ERROR_NOT_TRUSTED = "errorRequireTrustZone",
            HANDSHAKE_REQUIRED = "handshakeRequired",
            HANDSHAKE_ONLY = "handshakeOnly",
            NETWORK_SSID = "nwName",
            NETWORK_PIN = "pin",
            NETWORK_PASSWORD = "nwPwd",
            NETWORK_KEYMGMT = "ntKeyMgmt",
            NETWORK_BSSID = "bsid",
            NETWORK_ADDRESS_IP = "ipAdr",
            STUB = "stub"; // This keyword has meaning other than helping keep the communication process unblocked

    public enum Flavor
    {
        unknown,
        fossReliant,
        googlePlay
    }

    public static class Local
    {
        public static final String
                NETWORK_INTERFACE_UNKNOWN = "unk0",
                FILENAME_UNHANDLED_CRASH_LOG = "unhandled_crash_log.txt",
                SETTINGS_VIEWING = "sorting_settings";
    }
}
