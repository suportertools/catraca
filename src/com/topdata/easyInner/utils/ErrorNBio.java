package com.topdata.easyInner.utils;

import com.nitgen.SDK.BSP.NBioBSPJNI;

public class ErrorNBio {
    public static String getError(int error){
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_BASE_DEVICE){
            return "0 No Error";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INVALID_HANDLE){
            return "0x01 Invalid handle";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INVALID_POINTER){
            return "0x02 Invalid pointer";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INVALID_TYPE){
            return "0x03 Invalid type";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_FUNCTION_FAIL){
            return "0x04 Function failed";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_STRUCTTYPE_NOT_MATCHED){
            return "0x05 Input structure type not support";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_ALREADY_PROCESSED){
            return "0x06 The FIR data processed already";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_EXTRACTION_OPEN_FAIL){
            return "0x07 Extraction engine open fail";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_VERIFICATION_OPEN_FAIL){
            return "0x08 Verification engine open fail";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DATA_PROCESS_FAIL){
            return "0x09 Extraction fail";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_MUST_BE_PROCESSED_DATA){
            return "0x0a The FIR data must be process";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INTERNAL_CHECKSUM_FAIL){
            return "0x0b Invalid FIR data";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_ENCRYPTED_DATA_ERROR){
            return "0x0c FIR data encryption / decryption fail";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_UNKNOWN_FORMAT){
            return "0x0d Unknown FIR data format";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_UNKNOWN_VERSION){
            return "0x0e Unknown BSP version";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_VALIDITY_FAIL){
            return "0x0f BSP validity fail";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INIT_MAXFINGER){
            return "0x10 BSP Maxfinger option set fail";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INIT_SAMPLESPERFINGER){
            return "0x11 BSP Samplesperfinger option set fail";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INIT_VERIFYQUALITY){
            return "0x13 BSP VerifyQuality option set fail";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INIT_IDENTIFYQUALITY){
            return "0x14 BSP IdentifyQuality option set fail";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INIT_SECURITYLEVEL){
            return "0x15 BSP SecurityLevel option set fail";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INVALID_MINSIZE){
            return "0x16 Template data size error";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INVALID_TEMPLATE){
            return "0x17 Invalid template data";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_EXPIRED_VERSION){
            return "0x18 Expired BSP";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INVALID_SAMPLESPERFINGER){
            return "0x19 Invalid Samplesperfinger option value";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_UNKNOWN_INPUTFORMAT){
            return "0x1a Unknown INPUT_FIR type";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INIT_ENROLLSECURITYLEVEL){
            return "0x1b Invalid EnrollSecurityLevel option value";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INIT_NECESSARYENROLLNUM){
            return "0x1c Invalid NecessaryEnrollNum option value";
        }
        /*
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_OUT_OF_MEMORY){
            return "0x24 Out of memory";
        }
        */
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DEVICE_OPEN_FAIL){
            return "0x101 Device open fail";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INVALID_DEVICE_ID){
            return "0x102 Invalid device ID";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_WRONG_DEVICE_ID){
            return "0x103 Wrong device ID";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DEVICE_ALREADY_OPENED){
            return "0x104 Already open device";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DEVICE_NOT_OPENED){
            return "0x105 Device not opened";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DEVICE_BRIGHTNESS){
            return "0x106 Invalid Brightness option value";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DEVICE_CONTRAST){
            return "0x107 Invalid Contrast option value";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DEVICE_GAIN){
            return "0x108 Invalid Gain option value";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_LOWVERSION_DRIVER){
            return "0x109 Low version driver";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DEVICE_INIT_FAIL){
            return "0x10a Device initialize fail";
        }
        /*
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DEVICE_LOST_DEVICE){
            return "0x10b Device disconnected.";
        }
        
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DEVICE_DLL_LOAD_FAIL){
            return "0x10c Device module load fail.";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DEVICE_MAKE_INSTANCE_FAIL){
            return "0x10d Device Instance creation fail.";
        }
        
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DEVICE_DLL_GET_PROC_FAIL){
            return "0x10e Device function load fail.";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_DEVICE_IO_CONTROL_FAIL){
            return "0x10f Device IO fail.";
        }
        
        */
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_USER_CANCEL){
            return "0x201 Operation cancel from user";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_USER_BACK){
            return "0x202 Operation back from user";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_CAPTURE_TIMEOUT){
            return "0x203 Capture time out";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_CAPTURE_FAKE_SUSPICIOUS){
            return "0x204 Fake input occurred";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_ENROLL_EVENT_PLACE){
            return "0x205 Enroll method event";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_ENROLL_EVENT_HOLD){
            return "0x206 Enroll method event";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_ENROLL_EVENT_REMOVE){
            return "0x207 Enroll method event";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_ENROLL_EVENT_PLACE_AGAIN){
            return "0x208 Enroll method event";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_ENROLL_EVENT_EXTRACT){
            return "0x209 Enroll method event";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_ENROLL_EVENT_MATCH_FAILED){
            return "0x20a Enroll method event";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INIT_PRESEARCHRATE){
            return "0x501 Invalid PreSearchRate option value";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INDEXSEARCH_INIT_FAIL){
            return "0x502 IndexSearch engine initialize failed";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INDEXSEARCH_SAVE_DB){
            return "0x503 IndexSearch engine save db failed";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INDEXSEARCH_LOAD_DB){
            return "0x504 IndexSearch engine load db failed";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INDEXSEARCH_UNKNOWN_VER){
            return "0x505 Unknown IndexSearch engine version";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INDEXSEARCH_IDENTIFY_FAIL){
            return "0x506 IndexSearch engine identify failed";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INDEXSEARCH_DUPLICATED_ID){
            return "0x507 IndexSearch engine ID duplicated";
        }
        
        if (error == NBioBSPJNI.ERROR.NBioAPIERROR_INDEXSEARCH_IDENTIFY_STOP){
            return "0x508 IndexSearch engine identify stop from user";
        }
        
        // http://www.aqua-calc.com/convert/number/hexadecimal-to-decimal
        return "ERRO NÃO ENCONTRADO";
    }
}
