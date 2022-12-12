package com.keystone.coinlib.abi;

import com.keystone.coinlib.utils.FileUtil;
import com.keystone.coinlib.utils.SDCardUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SelfDefinedABIStore implements ABIStoreEngine {
    private static final String SELF_DEFINE_TFCARD_PATH = "contracts" + File.separator + "self_define";

    @Override
    public List<Contract> load(String address) {
        List<Contract> contracts = new ArrayList<>();
        String rootDir = SDCardUtil.externalSDCardPath() + File.separator + SELF_DEFINE_TFCARD_PATH;
        String rootFilePath = rootDir + File.separator + address + ".json";
        List<String> filePaths = new ArrayList<>();
        filePaths.add(rootFilePath);
        List<String> subDir = FileUtil.getAllSubDirPath(rootDir);
        for (String dirPath: subDir) {
            filePaths.add(dirPath + File.separator + address + ".json");
        }
        for (String path: filePaths) {
            Contract contract = getContract(path);
            if (contract != null) {
                contracts.add(contract);
            }
        }
        return contracts;
    }

    private Contract getContract(String filePath) {
        String content = FileUtil.readFromFile(filePath);
        try {
            Contract contract = new Contract();
            contract.setFromTFCard(true);
            JSONObject contentJson = new JSONObject(content);
            JSONObject metadataJson = contentJson.getJSONObject("metadata");
            JSONObject outputJson = metadataJson.getJSONObject("output");
            contract.setAbi(outputJson.getString("abi"));
            contract.setName(contentJson.getString("name"));
            return contract;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
