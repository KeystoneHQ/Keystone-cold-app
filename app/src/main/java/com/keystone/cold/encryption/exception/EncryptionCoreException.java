/*
 * Copyright (c) 2021 Keystone
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * in the file COPYING.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.keystone.cold.encryption.exception;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;

import org.json.JSONException;
import org.json.JSONObject;

public class EncryptionCoreException extends RuntimeException {
    private static final SparseArrayCompat<String> mDefineMap;

    static {
        mDefineMap = new SparseArrayCompat<>();
        mDefineMap.put(0x0100,	"ERT_INIT_FAIL");
        mDefineMap.put(0x0101,	"ERT_InitRngFail");
        mDefineMap.put(0x0102,	"ERT_InitFlashFail");
        mDefineMap.put(0x0103,	"ERT_InitUartFail");
        mDefineMap.put(0x0104,	"ERT_InitTimerFail");
        mDefineMap.put(0x0200,	"ERT_COMM_FAIL");
        mDefineMap.put(0x0201,	"ERT_CommTimeOut");
        mDefineMap.put(0x0202,	"ERT_CommInvalidCMD");
        mDefineMap.put(0x0203,	"ERT_CommFailEncrypt");
        mDefineMap.put(0x0204,	"ERT_CommFailLen");
        mDefineMap.put(0x0205,	"ERT_CommFailEtx");
        mDefineMap.put(0x0206,	"ERT_CommFailLrc");
        mDefineMap.put(0x0207,	"ERT_CommFailTLV");
        mDefineMap.put(0x0208,	"ERT_CommFailParam");
        mDefineMap.put(0x0300,	"ERT_BIP_FAIL");
        mDefineMap.put(0x0301,	"ERT_InvalidKey");
        mDefineMap.put(0x0302,	"ERT_GenKeyFail");
        mDefineMap.put(0x0303,	"ERT_ECDSASignFail");
        mDefineMap.put(0x0304,	"ERT_ECDSAVerifyFail");
        mDefineMap.put(0x0305,	"ERT_ED25519SignFail");
        mDefineMap.put(0x0306,	"ERT_ED25519VerifyFail");
        mDefineMap.put(0x0307,	"ERT_SecpEncryptFail");
        mDefineMap.put(0x0308,	"ERT_SecpDecryptFail");
        mDefineMap.put(0x0309,	"ERT_SM2EncryptFail");
        mDefineMap.put(0x030a,	"ERT_SM2DecryptFail");
        mDefineMap.put(0x030b,	"ERT_CKD_Fail");
        mDefineMap.put(0x030c,	"ERT_MnemonicNotMatch");
        mDefineMap.put(0x030d,	"ERT_CoinTypeInvalid");
        mDefineMap.put(0x030e,	"ERT_SignFail");
        mDefineMap.put(0x030f,	"ERT_VerifyFail");
        mDefineMap.put(0x0400,	"ERT_CMD_FAIL");
        mDefineMap.put(0x0401,	"ERT_NeedPreCMD");
        mDefineMap.put(0x0402,	"ERT_MsgNeedEncrypt");
        mDefineMap.put(0x0403,	"ERT_USERWithoutPermission");
        mDefineMap.put(0x0404,	"ERT_TLVArrayExceed");
        mDefineMap.put(0x0405,	"ERT_tlvArray_to_buf");
        mDefineMap.put(0x0406,	"ERT_HDPathIllegal");
        mDefineMap.put(0x0407,	"ERT_VerConflict");
        mDefineMap.put(0x0408,	"ERT_HDWalletSwitchNeed");
        mDefineMap.put(0x0409,	"ERT_HDWalletSwitchNotMatch");
        mDefineMap.put(0x040a,	"ERT_needEntropy");
        mDefineMap.put(0x0500,	"ERT_CHIP_FAIL");
        mDefineMap.put(0x0501,	"ERT_RngFail");
        mDefineMap.put(0x0502,	"ERT_SFlashFail");
        mDefineMap.put(0x0503,	"ERT_MallocFail");
        mDefineMap.put(0x0504,	"ERT_CheckSumFail");
        mDefineMap.put(0x0505,	"ERT_CheckMD5Fail");
        mDefineMap.put(0x0506,	"ERT_FuncParamInvalid");
        mDefineMap.put(0x0507,	"ERT_3DESFail");
        mDefineMap.put(0x0508,	"ERT_StorageFail");
        mDefineMap.put(0x0509,	"ERT_GetStatsFail");
        mDefineMap.put(0x050a,	"ERT_RecIDFail");
        mDefineMap.put(0x050b,	"ERT_UnexpectedFail");
        mDefineMap.put(0x050c,	"ERT_RSASubFail");
        mDefineMap.put(0x050d,	"ERT_LenTooLong");
        mDefineMap.put(0x050e,	"ERT_SNConflict");
        mDefineMap.put(0x050f,	"ERT_SNLenInvalid");
        mDefineMap.put(0x0510,	"ERT_SNInvalid");
        mDefineMap.put(0x0600,	"ERT_IAP_FAIL");
        mDefineMap.put(0x0601,	"ERT_FWUpdateFail");
        mDefineMap.put(0x0602,	"ERT_PacklenInvalid");
        mDefineMap.put(0x0603,	"ERT_IAP_fileDigest");
        mDefineMap.put(0x0604,	"ERT_IAP_beyoundRetry");
        mDefineMap.put(0x0700,	"ERT_UsrPassFAIL");
        mDefineMap.put(0x0701,	"ERT_needUsrPass");
        mDefineMap.put(0x0702,	"ERT_UsrPassVerifyFail");
        mDefineMap.put(0x0703,	"ERT_UsrPassNotCreate");
        mDefineMap.put(0x0704,	"ERT_UsrPassParaERR");
        mDefineMap.put(0x0705,	"ERT_needUsrFing");
        mDefineMap.put(0x0706,	"ERT_UsrFingVerifyFail");
        mDefineMap.put(0x0707,	"ERT_UsrFingNotCreate");
        mDefineMap.put(0x0708,	"ERT_UsrFingParaERR");
        mDefineMap.put(0x0709,	"ERT_needMessageSign");
        mDefineMap.put(0x070a,	"ERT_needToken");
        mDefineMap.put(0x070b,	"ERT_TokenVerifyFail");
        mDefineMap.put(0x070c,	"ERT_UsrSettingsLoadFail");
        mDefineMap.put(0x070d,	"ERT_UsrSettingsStoreFail");
        mDefineMap.put(0x070e,	"ERT_UsrSettingsNotAllow");
        mDefineMap.put(0x0800,	"ERT_Verify_Init");
        mDefineMap.put(0x0801,	"ERT_VerifyValueFail");
        mDefineMap.put(0x0802,	"ERT_VerifyLenFail");
        mDefineMap.put(0x5AA5,	"ERT_Verify_Success");
        mDefineMap.put(0xFF00,	"ERT_DebugInvalid");
        mDefineMap.put(0xFFAA,	"ERT_UnderAttack");
        mDefineMap.put(0xFFFF,	"ERT_Unauthorized");
        mDefineMap.put(0xFFFF,	"ERT_Total");
    }

    private int errorCode;
    private String errorMessage;

    public EncryptionCoreException(int code, @Nullable String error) {
        super(toJsonString(code, error));
        this.errorCode = code;
        this.errorMessage = error;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @NonNull
    private static String toJsonString(int code, @Nullable String error) {
        final JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("code", code);
            jsonObject.put("define", mDefineMap.get(code, "Not Found"));
            jsonObject.put("error", TextUtils.isEmpty(error) ? "empty error message" : error);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject.toString();
    }
}
