package modoo.front.cmm.web;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.SessionAttributes;

import egovframework.com.cmm.LoginVO;
import egovframework.com.uat.uia.service.EgovLoginService;
import modoo.module.common.util.SiteDomainHelper;
import modoo.module.common.web.CommonDefaultController;
import modoo.module.mber.agre.service.MberAgreService;
import modoo.module.mber.agre.service.MberAgreVO;
import modoo.module.mber.info.service.MberService;
import modoo.module.mber.info.service.MberVO;
import modoo.module.mber.sso.service.Ezwel;
import modoo.module.mber.sso.service.EzwelCryptoModule;
import modoo.module.site.service.SiteService;
import modoo.module.site.service.SiteVO;

@Controller
public class PrtnrSsoController extends CommonDefaultController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrtnrSsoController.class);
	
	@Resource(name = "mberService")
	private MberService mberService;
	
	/** EgovLoginService */
	@Resource(name = "loginService")
	private EgovLoginService loginService;
	
	@Resource(name = "mberAgreService")
	private MberAgreService mberAgreService;
	
	@Resource(name = "siteService")
	private SiteService siteService;
	
	/**
	 * 제휴사 : 이지웰 
	 * @param request
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/prtnr/ezwel/index.do")
	public String ezwelIndex(HttpServletRequest request, Model model) throws Exception {
		model.addAttribute("domain", SiteDomainHelper.getDomain());
		EzwelCryptoModule dec = new EzwelCryptoModule();
		HttpSession session = request.getSession();
		/*Decoder decoder = Base64.getDecoder();
		String cspCd = "";
		byte[] encryptbytes = decoder.decode(request.getParameter("cspCd"));
		cspCd = new String(encryptbytes);*/
		
		Ezwel ezwelUserInfo = new Ezwel();
		/*ezwelUserInfo.setCspCd(cspCd);*/
		ezwelUserInfo.setClientCd(dec.decode(request.getParameter("clientCd")));
		ezwelUserInfo.setUserKey(dec.decode(request.getParameter("userKey")));
		ezwelUserInfo.setUserNm(dec.decode(request.getParameter("userNm")));
		ezwelUserInfo.setGoUrl(dec.decode(request.getParameter("goUrl")));
		if(StringUtils.isNotEmpty(request.getParameter("ezmilYn"))) {
			ezwelUserInfo.setEzmilYn(dec.decode(request.getParameter("ezmilYn")).substring(0,1));
		}else {
			LOGGER.info("EzmilYn 값이 없음.");
		}
		if(StringUtils.isNotEmpty(dec.decode(request.getParameter("pointYn")))) {
			ezwelUserInfo.setPointYn(dec.decode(request.getParameter("pointYn")).substring(0,1));
		}else {
			LOGGER.info("PointYn 값이 없음.");
		}
		if(StringUtils.isNotEmpty(dec.decode(request.getParameter("specialUseYn")))) {
			ezwelUserInfo.setSpecialUseYn(dec.decode(request.getParameter("specialUseYn")).substring(0,1));
		}else {
			LOGGER.info("SpecialUseYn 값이 없음.");
		}
		ezwelUserInfo.setSpecialPointNm(dec.decode(request.getParameter("specialPointNm")));
		ezwelUserInfo.setReceiptYn(dec.decode(request.getParameter("receiptYn")));
		
		if(StringUtils.isEmpty(ezwelUserInfo.getUserKey())) {
			model.addAttribute("errCode", "ERR01");
			LOGGER.info("sso 연결없이 들어왔거나 session에 정보가 없습니다.");
			return "modoo/front/prtnr/sso/common/ssoError";
		}
		
		//System.out.println(ezwelUserInfo.toString());
		LOGGER.info("EZWEL SSO USER : " + ezwelUserInfo.toString());
		// Ezwel [cspCd=ksubes, clientCd=test, userKey=1007426847, userNm=이세행, goUrl=, pointYn=Y              , ezmilYn=Y              , specialUseYn=N              , specialPointNm=특별포인트, receiptYn=NNNN]
		session.setAttribute("ssoUser", ezwelUserInfo);

		return "modoo/front/prtnr/sso/ezwel/index";
	}
	
	//제휴사 테스트
	@RequestMapping(value = "/prtnr/ezwel/indexTest.do")
	public String ezwelIndexTest(HttpServletRequest request, Model model) throws Exception {
		model.addAttribute("domain", SiteDomainHelper.getDomain());
		EzwelCryptoModule dec = new EzwelCryptoModule();
		HttpSession session = request.getSession();
		
		Ezwel ezwelUserInfo = new Ezwel();
		/*ezwelUserInfo.setCspCd(cspCd);*/
		ezwelUserInfo.setClientCd("test");
		ezwelUserInfo.setUserKey("1007426847");
		ezwelUserInfo.setUserNm("이세행");
		ezwelUserInfo.setGoUrl("");
		ezwelUserInfo.setEzmilYn("Y");
		ezwelUserInfo.setPointYn("N");
		ezwelUserInfo.setSpecialUseYn("N");
		ezwelUserInfo.setSpecialPointNm("특별포인트");
		ezwelUserInfo.setReceiptYn("NNNN");
		session.setAttribute("ssoUser", ezwelUserInfo);

		return "modoo/front/prtnr/sso/ezwel/index";
	}
	
	/**
	 * 제휴사 회원 검증
	 * @param request
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/prtnr/sso/modooIndex.do")
	public String modooIndex(HttpServletRequest request, Model model) throws Exception {
		
		HttpSession session =request.getSession();
		/*HttpSession session = request.getSession();*/
		Ezwel ezwelUserInfo = (Ezwel)session.getAttribute("ssoUser");
		if(ezwelUserInfo == null || StringUtils.isEmpty(ezwelUserInfo.getUserKey())) {
			// sso 연결없이 들어왔거나 session에 담기지 않을때 
			model.addAttribute("errCode", "ERR01");
			LOGGER.info("sso 연결없이 들어왔거나 session에 정보가 없습니다.");
			return "modoo/front/prtnr/sso/common/ssoError";
		}else {
			MberVO mber = new MberVO();
			mber.setClientCd(ezwelUserInfo.getClientCd());
			mber.setUserKey(ezwelUserInfo.getUserKey());
			
			int checkCnt = mberService.selectSsoMemberCheck(mber);
			
			if(checkCnt > 0) { 
				
				MberVO mb = mberService.selectSsoMember(mber);
				
				// 로그인 처리
				LoginVO loginVO = new LoginVO();
				loginVO.setUniqId(mb.getEsntlId());
				LoginVO resultVO = loginService.actionLoginByEsntlId(loginVO);
				
				request.getSession().setAttribute("loginVO", resultVO);
				request.getSession().setAttribute("accessUser", resultVO.getUserSe().concat(resultVO.getId()));
				
				//이지웰 상태 변경
				MberVO ezMber = new MberVO();
				ezMber.setClientCd(ezwelUserInfo.getClientCd());
				ezMber.setUserKey(ezwelUserInfo.getUserKey());
				ezMber.setEzmilYn(ezwelUserInfo.getEzmilYn());
				ezMber.setPointYn(ezwelUserInfo.getPointYn());
				ezMber.setSpecialUseYn(ezwelUserInfo.getSpecialUseYn());
				ezMber.setReceiptYn(ezwelUserInfo.getReceiptYn());
				mberService.updateEzwelMember(ezMber);
				
				return "redirect:/index.do";
				
			}else {
				// 회원 동의 페이지 이동
				return "redirect:/prtnr/sso/mberAgre.do";
			}
			
		}
	}
	
	/**
	 * 제휴사 회원약관동의 폼
	 * @param request
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/prtnr/sso/mberAgre.do", method = RequestMethod.GET)
	public String mberAgre(HttpServletRequest request, Model model) throws Exception {
		HttpSession session = request.getSession();
		
		//이지웰
		Ezwel ezwelUserInfo = (Ezwel) session.getAttribute("ssoUser");
		//SNS로그인 시
		LoginVO loginInfo = (LoginVO) session.getAttribute("memberVO");
		if((ezwelUserInfo == null || StringUtils.isEmpty(ezwelUserInfo.getUserKey())) && loginInfo == null) {
			// sso 연결없이 들어왔거나 session에 담기지 않을때 
			model.addAttribute("errCode", "ERR01");
			LOGGER.info("sso 연결없이 들어왔거나 session에 정보가 없습니다.");
			return "modoo/front/prtnr/sso/common/ssoError";
		}

		SiteVO site = new SiteVO();
		site.setSiteId(SiteDomainHelper.getSiteId());
		SiteVO vo = siteService.selectSite(site);
		model.addAttribute("site", vo);
		
		model.addAttribute("mberAgre", new MberAgreVO());
		
		return "modoo/front/prtnr/sso/common/mberAgre";
	}
	
	/**
	 * 제휴사 사용자 약관동의 
	 * @param mberAgre
	 * @param request
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/prtnr/sso/mberAgre.do", method = RequestMethod.POST)
	public String mberAgre(@ModelAttribute("mberAgre") MberAgreVO mberAgre, HttpServletRequest request, Model model) throws Exception {
		HttpSession session = request.getSession();
		Ezwel ezwelUserInfo = (Ezwel) session.getAttribute("ssoUser");
		if(mberAgre.getTermsCondAt().equals("Y") && mberAgre.getPrivInfoAt().equals("Y")) {
			String clientCd = "";
			String userKey = "";
			//SNS로그인 시
			LoginVO loginInfo = (LoginVO) session.getAttribute("memberVO");
			if((ezwelUserInfo == null || StringUtils.isEmpty(ezwelUserInfo.getUserKey())) && loginInfo == null) {
				// sso 연결없이 들어왔거나 session에 담기지 않을때 
				model.addAttribute("errCode", "ERR01");
				LOGGER.info("sso 연결없이 들어왔거나 session에 정보가 없습니다.");
				return "modoo/front/prtnr/sso/common/ssoError";
			}
			
			// 회원 생성
			MberVO vo = new MberVO();
			vo.setSiteId(SiteDomainHelper.getSiteId());
			if(ezwelUserInfo != null && StringUtils.isNotEmpty(ezwelUserInfo.getUserKey())){
				vo.setMberId(ezwelUserInfo.getClientCd() + ezwelUserInfo.getUserKey());
				vo.setMberNm(ezwelUserInfo.getUserNm());
				vo.setEmail(null);
				vo.setMberSttus("P");
				vo.setMberTyCode("USR01");
				vo.setClientCd(ezwelUserInfo.getClientCd());
				vo.setUserKey(ezwelUserInfo.getUserKey());
				vo.setPointYn(ezwelUserInfo.getPointYn());
				vo.setEzmilYn(ezwelUserInfo.getEzmilYn());
				vo.setSpecialUseYn(ezwelUserInfo.getSpecialUseYn());
				vo.setReceiptYn(ezwelUserInfo.getReceiptYn());
				vo.setSexdstn("E");
				vo.setGroupId("GROUP_00000000000001"); // EZWEL
				vo.setAuthorCode("ROLE_USER");
				
				clientCd = ezwelUserInfo.getClientCd();
				userKey = ezwelUserInfo.getUserKey();
			//SNS로 로그인 시
			}else if(loginInfo != null){
				vo.setMberNm(loginInfo.getName());
				vo.setEmail(loginInfo.getEmail());
				vo.setMberSttus("P");
				vo.setMberTyCode("USR01");
				vo.setClientCd(loginInfo.getClientCd());
				vo.setUserKey(loginInfo.getId());
				vo.setAuthorCode("ROLE_USER");
				vo.setSexdstn(loginInfo.getSexdstn());
				vo.setAgrde(loginInfo.getAgrde());
				//vo.setBirthday(loginInfo.getBirthday());
				//vo.setMoblphon(loginInfo.getMoblphon());
				if("KAKAO".equals(loginInfo.getClientCd())){
					vo.setMberId("KAKAO" + loginInfo.getId());
					vo.setPassword("KAKAO-" + loginInfo.getId());
					vo.setGroupId("GROUP_00000000000002");
				}else if("GOOGLE".equals(loginInfo.getClientCd())){
					vo.setMberId(loginInfo.getEmail());
					vo.setPassword("GOOGLE-" + loginInfo.getId());
					vo.setGroupId("GROUP_00000000000003");
				}else if("APPLE".equals(loginInfo.getClientCd())){
					vo.setMberId("APPLE" + loginInfo.getId());
					vo.setPassword("APPLE-" + loginInfo.getId());
					vo.setGroupId("GROUP_00000000000004");
					vo.setSexdstn("E");
				}else if("NAVER".equals(loginInfo.getClientCd())){
					vo.setMberId("NAVER" + loginInfo.getId());
					vo.setPassword("NAVER-" + loginInfo.getId());
					vo.setGroupId("GROUP_00000000000005");
				}
				
				clientCd = loginInfo.getClientCd();
				userKey = loginInfo.getId();
			}
			
			//약관 동의
			String esntlId = mberService.insertSsoMber(vo);
			mberAgre.setEsntlId(esntlId);
			mberAgreService.insertMberAgre(mberAgre);
			
			MberVO mber = new MberVO();
			mber.setClientCd(clientCd);
			mber.setUserKey(userKey);
			
			MberVO mb = mberService.selectSsoMember(mber);
			
			// 로그인 처리
			LoginVO loginVO = new LoginVO();
			loginVO.setUniqId(mb.getEsntlId());
			LoginVO resultVO = loginService.actionLoginByEsntlId(loginVO);
			
			request.getSession().setAttribute("loginVO", resultVO);
			request.getSession().setAttribute("accessUser", resultVO.getUserSe().concat(resultVO.getId()));
			
			return "redirect:/index.do";
		}else {
			model.addAttribute("message", "필수 항목을 확인하세요");
			return "modoo/front/prtnr/sso/common/mberAgre";
			
		}
		
	}

}
