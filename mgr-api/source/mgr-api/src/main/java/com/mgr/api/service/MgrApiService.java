package com.mgr.api.service;

import com.mgr.api.model.Permission;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class MgrApiService {
    @Autowired
    private CommonAsyncService commonAsyncService;

    private Map<String, Long> storeQRCodeRandom = new ConcurrentHashMap<>();

    public void deleteFile(String filePath) {
        //call to mediaService for delete
    }

    public void sendEmail(String email, String msg, String subject, boolean html) {
        commonAsyncService.sendEmail(email, msg, subject, html);
    }


    public String convertGroupToUri(List<Permission> permissions) {
        if (permissions != null) {
            StringBuilder builderPermission = new StringBuilder();
            for (Permission p : permissions) {
                builderPermission.append(p.getAction().trim().replace("/v1", "") + ",");
            }
            return builderPermission.toString();
        }
        return null;
    }

    public synchronized boolean checkCodeValid(String code) {
        //delelete key has valule > 60s
        Set<String> keys = storeQRCodeRandom.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            Long value = storeQRCodeRandom.get(key);
            if ((System.currentTimeMillis() - value) > 60000) {
                storeQRCodeRandom.remove(key);
            }
        }

        if (storeQRCodeRandom.containsKey(code)) {
            return false;
        }
        storeQRCodeRandom.put(code, System.currentTimeMillis());
        return true;
    }
}
