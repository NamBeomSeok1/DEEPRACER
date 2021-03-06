package egovframework.com.uat.uia.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import egovframework.com.uat.uia.service.AppleService;
import egovframework.com.uat.uia.service.Payload;
import egovframework.com.uat.uia.service.TokenResponse;
import egovframework.com.utl.fcc.service.AppleUtils;

import java.util.Map;

@Service("appleService")
public class AppleServiceImpl implements AppleService {

    @Autowired
    AppleUtils appleUtils;

    /**
     * 유효한 id_token인 경우 client_secret 생성
     *
     * @param id_token
     * @return
     */
    @Override
    public String getAppleClientSecret(String id_token) {
        if(appleUtils.verifyIdentityToken(id_token)) {
            return appleUtils.createClientSecret();
        }
        return null;
    }

    /**
     * code 또는 refresh_token가 유효한지 Apple Server에 검증 요청
     *
     * @param client_secret
     * @param code
     * @param refresh_token
     * @return
     */
    @Override
    public TokenResponse requestCodeValidations(String client_secret, String code, String refresh_token) {

        TokenResponse tokenResponse = new TokenResponse();

        if(client_secret != null && code != null && refresh_token == null) {
            tokenResponse = appleUtils.validateAuthorizationGrantCode(client_secret, code);
        }else if(client_secret != null && code == null && refresh_token != null) {
            tokenResponse = appleUtils.validateAnExistingRefreshToken(client_secret, refresh_token);
        }

        return tokenResponse;
    }

    /**
     * Apple login page 호출을 위한 Meta 정보 가져오기
     *
     * @return
     */
    @Override
    public Map<String, String> getLoginMetaInfo() {
        return appleUtils.getMetaInfo();
    }

    /**
     * id_token에서 payload 데이터 가져오기
     *
     * @return
     */
    /*
    @Override
    public String getPayload(String id_token) {
        return appleUtils.decodeFromIdToken(id_token).toString();
    }
    */
    public Payload getPayload(String id_token) {
        return appleUtils.decodeFromIdToken(id_token);
    }
}
