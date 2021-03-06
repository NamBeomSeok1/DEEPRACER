package modoo.front.qainfo.web;

import java.io.File;
import java.util.List;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartRequest;

import egovframework.com.cmm.ComDefaultCodeVO;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.service.EgovCmmUseService;
import egovframework.com.cmm.service.EgovFileMngService;
import egovframework.com.cmm.service.FileVO;
import egovframework.com.utl.fcc.service.EgovDateUtil;
import egovframework.rte.fdl.access.service.EgovUserDetailsHelper;
import egovframework.rte.psl.dataaccess.util.EgovMap;
import egovframework.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import modoo.module.biztalk.service.BiztalkService;
import modoo.module.biztalk.service.BiztalkVO;
import modoo.module.common.service.FileMngUtil;
import modoo.module.common.service.JsonResult;
import modoo.module.common.util.CommonUtils;
import modoo.module.common.util.SiteDomainHelper;
import modoo.module.common.web.CommonDefaultController;
import modoo.module.qainfo.service.QainfoService;
import modoo.module.qainfo.service.QainfoVO;
import modoo.module.shop.cmpny.service.CmpnyService;
import modoo.module.shop.cmpny.service.CmpnyVO;
import modoo.module.shop.goods.info.service.GoodsService;
import modoo.module.shop.goods.info.service.GoodsVO;

@Controller("frontQaInfoController")
public class QainfoController extends CommonDefaultController{

	private static final Logger LOGGER =  LoggerFactory.getLogger(QainfoController.class);
	
	@Resource(name="qainfoService")
	private QainfoService qainfoService;
	
	@Resource(name="EgovCmmUseService")
	private EgovCmmUseService cmmUseService;
	
	@Resource(name = "goodsService")
	private GoodsService goodsService;
	
	@Resource(name = "fileMngUtil")
	private FileMngUtil fileMngUtil;
	
	@Resource(name = "EgovFileMngService")
	private EgovFileMngService fileMngService;

	@Resource(name = "biztalkService")
	private BiztalkService biztalkService;
	
	@Resource(name = "cmpnyService") 
	private CmpnyService cmpnyService;
	
	/*
	 * ????????? ?????? ??????
	 *@param searchVO
	 *@param request
	 *@return 
	 */
	@RequestMapping(value="/qainfo/qainfoList.json")
	@ResponseBody
	public JsonResult qainfoList(QainfoVO searchVO,HttpServletRequest request){
		
		JsonResult jsonResult = new JsonResult();
		
		LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
		
		try{
			if(StringUtils.isEmpty(searchVO.getSiteId())){
				searchVO.setSiteId(SiteDomainHelper.getSiteId());
			}
			
			PaginationInfo paginationInfo = new PaginationInfo();
			searchVO.setPageUnit(propertiesService.getInt("gridPageUnit"));
			int totalRecordcount = qainfoService.selectQainfoListCnt(searchVO);
			List<EgovMap> resultList = (List<EgovMap>) qainfoService.selectQainfoList(searchVO);
			
			
			//?????? qna?????????
			if(request.getParameter("goodsId")!=null){
				searchVO.setGoodsId(request.getParameter("goodsId"));
				searchVO.setQaSeCode("GOODS");
				
				ComDefaultCodeVO codeVO = new ComDefaultCodeVO();
				codeVO.setCodeId("CMS018");
				List<?> qestnTyCodeList = cmmUseService.selectCmmCodeDetail(codeVO);
				jsonResult.put("qestnTyCodeList", qestnTyCodeList);
				
				this.setPagination(paginationInfo, searchVO);
				totalRecordcount=qainfoService.selectQainfoListCnt(searchVO);
				if(totalRecordcount<10){
					searchVO.setFirstIndex(0);
				}
				resultList = (List<EgovMap>) qainfoService.selectQainfoList(searchVO);
				paginationInfo.setTotalRecordCount(totalRecordcount);
				
				//?????? ***??????
				for(EgovMap qa: resultList){
					boolean isLogin = false;
					String qestnCn = null;
					qa.put("wrterNm",(qa.get("wrterId").toString().substring(0,1)+"****"+qa.get("wrterId").toString().substring(qa.get("wrterId").toString().length()-1,qa.get("wrterId").toString().length())));
					
					if(user==null || !user.getUniqId().equals(qa.get("frstRegisterId"))){
						qestnCn=("Y".equals(qa.get("secretAt"))) ? "?????? ????????????." : (String) qa.get("qestnCn");
						isLogin = false;
					}else if(qa.get("frstRegisterId").equals(user.getUniqId())){
						qestnCn=(String)qa.get("qestnCn");
						isLogin = true;
					}
					qa.put("qestnCn", qestnCn);
					qa.put("isLogin", isLogin);
				}
			}
			
			jsonResult.put("paginationInfo", paginationInfo);
			jsonResult.put("list", resultList);
			
			jsonResult.setSuccess(true);
			
		}catch (Exception e) {
			loggerError(LOGGER, e);
			jsonResult.setSuccess(false);
			jsonResult.setMessage(egovMessageSource.getMessage("fail.common.select"));
		}
		return jsonResult;
	}
	
	 /* ?????? QNA ???
	 * @param searchVO
	 * @param model
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value="/shop/goods/qainfo/qaInfo.do")
	public String moveQainfo(@ModelAttribute("qaInfo") QainfoVO searchVO, Model model,HttpServletRequest request) throws Exception {
				
		/*	GoodsVO goods = new GoodsVO();
			goods.setGoodsId(request.getParameter("goodsId"));
			GoodsVO goodsInfo = goodsService.selectGoods(goods);
			model.addAttribute("goods",goodsInfo);*/
			
			ComDefaultCodeVO codeVO = new ComDefaultCodeVO();
			codeVO.setCodeId("CMS018");
			List<?> qestnTyCodeList = cmmUseService.selectCmmCodeDetail(codeVO);
			model.addAttribute("qestnTyCodeList",qestnTyCodeList);
				
			return "modoo/front/shop/goods/info/qaInfo/qainfo";
	}
	/* ?????? QNA ????????????
	 * @param searchVO
	 * @param model
	 * @param request
	 * @return
	 */
	
	@RequestMapping(value="/shop/goods/qainfo/qaInfoList.do")
	public String qainfoList(@ModelAttribute("qaInfo") QainfoVO searchVO, Model model,HttpServletRequest request) throws Exception {
		
		GoodsVO goods = new GoodsVO();
		goods.setGoodsId(request.getParameter("goodsId"));
		GoodsVO goodsInfo = goodsService.selectGoods(goods);
		
		PaginationInfo paginationInfo = new PaginationInfo();
		searchVO.setPageUnit(propertiesService.getInt("gridPageUnit"));
		searchVO.setGoodsId(request.getParameter("goodsId"));
		
		this.setPagination(paginationInfo, searchVO);
		int totalRecordcount = qainfoService.selectQainfoListCnt(searchVO);
		paginationInfo.setTotalRecordCount(totalRecordcount);
		
		model.addAttribute("paginationInfo", paginationInfo);
		model.addAttribute("goods",goodsInfo);
		
		return "modoo/front/shop/goods/info/qaInfo/qaInfoList";
	}
	
	
	 /* ?????? QNA ??????
	 * @param searchVO
	 * @return
	 */
	@RequestMapping("/qaInfo/qaInfoDetail.json")
	@ResponseBody
	public JsonResult detailQaInfo(QainfoVO searchVO,@RequestParam("qaId")String qaId){
		
		JsonResult jsonResult = new JsonResult();
		
		
		
		
		
		// TODO : ??????????????? ?????? ????????? ?????????????????? ?????? ??????????????????.
		
		
		
		
		
		
		
		
		
		
		
		try {
			searchVO.setQaId(qaId);
			searchVO = qainfoService.selectQainfo(searchVO);
			List<?> imgs = qainfoService.selectImageList(searchVO);
			if(searchVO!=null){
				
				jsonResult.put("imgs", imgs);
				jsonResult.put("qaInfo", searchVO);
				jsonResult.setMessage(egovMessageSource.getMessage("success.common.select"));
				jsonResult.setSuccess(true);
				
			}
		} catch (Exception e) {
			loggerError(LOGGER, e);
			jsonResult.setSuccess(false);
			jsonResult.setMessage(egovMessageSource.getMessage("fail.common.select"));
		}
		
		return jsonResult;
		
	}
	
	/*
	 * ????????? ?????? ??????
	 * @param qainfo
	 * @param bindingResult
	 * @return
	 */
	@RequestMapping(value="/qainfo/regstQainfo.json", method=RequestMethod.POST)
	@ResponseBody
	public JsonResult writeQainfo(@Valid QainfoVO qainfo, BindingResult bindingResult, MultipartHttpServletRequest multiRequest){
		
		JsonResult jsonResult = new JsonResult();
		
		Boolean isLogin = EgovUserDetailsHelper.isAuthenticated();
		
		if(isLogin){
			LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();

			try {
				if(!this.isHasErrorsJson(bindingResult, jsonResult,"<br/>")){
					
					if(StringUtils.isEmpty(qainfo.getSiteId())){
						qainfo.setSiteId(SiteDomainHelper.getSiteId());
					}
					
					jsonResult.setSuccess(true);
					
					if(qainfo.getGoodsId()!=null){
						qainfo.setQaSeCode("GOODS"); //????????????
						QainfoVO searchVO = new QainfoVO();
						searchVO.setWrterId(user.getId());
						searchVO.setGoodsId(qainfo.getGoodsId());
						int chkQaCnt = qainfoService.selectQainfoListCnt(searchVO);
						if(chkQaCnt>0){
							jsonResult.setSuccess(false);
							jsonResult.setMessage("??? ????????? ????????? ????????? ??? ??? ????????????.");
						}
						if(qainfo.getSecretAt()==null)qainfo.setSecretAt("N");
						qainfo.setQestnSj(CommonUtils.unscript(qainfo.getQestnSj()));
					
					}else if(qainfo.getGoodsId()==null){
						
						qainfo.setQaSeCode("SITE");  //1:1??????
						//?????? ?????????
						final List<MultipartFile> fileList = multiRequest.getFiles("atchFile");

						System.out.println("??????"+fileList);
						
						String atchFileId = "";
						if(!fileList.isEmpty()) {
							
							String prefixPath = "QNA";
							List<FileVO> files = fileMngUtil.parseFileInf(fileList, "QNA_", 0, "", "", prefixPath); // ???????????? : src/main/resources/egovframework/egovProps/globals.properties -> Globals.fileStorePath ??????
							atchFileId = fileMngService.insertFileInfs(files);
							qainfo.setAtchFileId(atchFileId); // ??????????????????ID
						}
					}
					if(StringUtils.isNotEmpty(qainfo.getTelno1()) && StringUtils.isNotEmpty(qainfo.getTelno2()) && StringUtils.isNotEmpty(qainfo.getTelno3())){
						qainfo.setWrterTelno(qainfo.getTelno1()+"-"+qainfo.getTelno2()+"-"+qainfo.getTelno3());
					}
					qainfo.setQestnTyCode(qainfo.getQestnTyCode());
					qainfo.setQestnCn(CommonUtils.unscript(qainfo.getQestnCn()));
					qainfo.setFrstRegisterId(user.getUniqId());
					qainfo.setEmailAdres(user.getEmail());
					qainfo.setWrterNm(user.getName());
					qainfo.setWrterId(user.getId());
					qainfo.setWritngDe(CommonUtils.validChkDateTime(EgovDateUtil.getToday()+"000000"));
					qainfo.setFrstRegistPnttm(EgovDateUtil.getToday());
					qainfo.setQnaProcessSttusCode("R");
					if(jsonResult.isSuccess()){
						qainfoService.insertQainfo(qainfo);
						jsonResult.setSuccess(true);
						//jsonResult.setMessage(egovMessageSource.getMessage("success.common.insert"));
						if("SITE".equals(qainfo.getQaSeCode())) {
							jsonResult.setMessage("1:1????????? ?????????????????????.");
						}else if("GOODS".equals(qainfo.getQaSeCode())) {
							jsonResult.setMessage("Q&A??? ??????????????? ?????????????????????.");
						}else {
							jsonResult.setMessage("??????????????? ?????????????????????.");
						}
					}
					
					BiztalkVO bizTalk = new BiztalkVO();
					/* ?????? ?????? ?????? ????????? */
					if ("GOODS".equals(qainfo.getQaSeCode())) {
						GoodsVO goods = new GoodsVO();
						goods.setGoodsId(qainfo.getGoodsId());
						GoodsVO goodsInfo = goodsService.selectGoods(goods);
						
						CmpnyVO cmpny = new CmpnyVO();
						cmpny.setCmpnyId(goodsInfo.getCmpnyId());
						CmpnyVO cmpnyInfo = cmpnyService.selectCmpny(cmpny);
						bizTalk = new BiztalkVO();
						bizTalk.setRecipient(cmpnyInfo.getChargerTelno());
						bizTalk.setTmplatCode("template_015");
						/*[???????????????] ????????? ?????????????????????. * ????????? : #{PRODUCTNAME}
						 * 
						 */
						BiztalkVO template = biztalkService.selectBizTalkTemplate(bizTalk);
						String message = template.getTmplatCn();
						message = message.replaceAll("#\\{PRODUCTNAME\\}", qainfo.getFrstRegistPnttm());
						bizTalk.setMessage(message);
					}
					/* 1:1 ?????? ?????? ????????? */
					else if ("SITE".equals(qainfo.getQaSeCode())) {
						
						CmpnyVO cmpny = new CmpnyVO();
						cmpny.setCmpnyId("CMPNY_00000000000042");
						CmpnyVO cmpnyInfo = cmpnyService.selectCmpny(cmpny);
						bizTalk = new BiztalkVO();
						bizTalk.setRecipient(cmpnyInfo.getChargerTelno());
						bizTalk.setTmplatCode("template_018");
						/*[???????????????] 1:1????????? ?????????????????????.
						 * ???????????? : #{REGDATETIME}
						 */
						BiztalkVO template = biztalkService.selectBizTalkTemplate(bizTalk);
						String message = template.getTmplatCn();
						message = message.replaceAll("#\\{REGDATETIME\\}", qainfo.getFrstRegistPnttm());
						bizTalk.setMessage(message);
						
						/* ??????, ?????? ???????????? ?????? ??? ?????? ?????? */
						BiztalkVO result = biztalkService.sendAlimTalk(bizTalk);
						bizTalk.setRecipient("01048061787");
					}
					
					BiztalkVO result = biztalkService.sendAlimTalk(bizTalk);
				
				}
			} catch (Exception e) {
				LOGGER.error("Exception: "+e);
				jsonResult.setMessage(egovMessageSource.getMessage("fail.common.insert"));
				jsonResult.setSuccess(false);
			}
		}else{
			jsonResult.setMessage(egovMessageSource.getMessage("fail.common.login"));
		}
			return jsonResult;
	}

	/*
	 * ????????? ?????? ??????
	 * @param qainfo
	 * @param bindingResult
	 * @return
	 */
	@RequestMapping(value="/qainfo/updateQainfo.json", method=RequestMethod.POST)
	@ResponseBody
	public JsonResult updateQainfo(@Valid QainfoVO qainfo, BindingResult bindingResult, MultipartRequest multiRequest){
	
		JsonResult jsonResult = new JsonResult();
		
		Boolean isLogin = EgovUserDetailsHelper.isAuthenticated();
		
		if(isLogin){
			LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
		
			try {
				if(!this.isHasErrorsJson(bindingResult, jsonResult,"<br/>")){
					
					if(qainfo.getGoodsId()!=null){
						qainfo.setQestnTyCode(qainfo.getQestnTyCode());
						if(qainfo.getSecretAt()==null)qainfo.setSecretAt("N");
					
					}else if(qainfo.getGoodsId()==null){
						qainfo.setQaSeCode("SITE");  //1:1??????
						
						//?????? ?????????
						final List<MultipartFile> fileList = multiRequest.getFiles("atchFile");

						for (MultipartFile file : fileList) {
							System.out.println(file.getOriginalFilename());
						}
						
						String atchFileId = qainfo.getAtchFileId();
						String prefixPath = "QNA";
						if(!fileList.isEmpty()) {
							
							if(StringUtils.isEmpty(atchFileId)){
								List<FileVO> files = fileMngUtil.parseFileInf(fileList, "QNA_", 0, "", "", prefixPath); // ???????????? : src/main/resources/egovframework/egovProps/globals.properties -> Globals.fileStorePath ??????
								atchFileId = fileMngService.insertFileInfs(files);
								qainfo.setAtchFileId(atchFileId); // ??????????????????ID
							}else{
								FileVO fvo = new FileVO();
								fvo.setAtchFileId(atchFileId);
								int cnt = fileMngService.getMaxFileSN(fvo);
								List<FileVO> files = fileMngUtil.parseFileInf(fileList, "QNA_", cnt, atchFileId, "", prefixPath);
								fileMngService.updateFileInfs(files);
							}
						}
						//?????? ????????? ???
					}
					
					// TODO : ????????? ??????????????? ID, ????????? ?????? ?????????????????? ???????????? ?????? ???????????????. ????????? ????????? ?????? ????????? WRTER_NM, WRTER_ID, EMAIL??? ???????????? ????????? ??????????????????.
					qainfo.setWrterNm(user.getName());
					qainfo.setWrterId(user.getId());
					qainfo.setEmailAdres(user.getEmail());
					
					if(StringUtils.isNotEmpty(qainfo.getTelno1()) && StringUtils.isNotEmpty(qainfo.getTelno2()) && StringUtils.isNotEmpty(qainfo.getTelno3())){
						qainfo.setWrterTelno(qainfo.getTelno1()+"-"+qainfo.getTelno2()+"-"+qainfo.getTelno3());
					}
					
					qainfo.setQestnSj(CommonUtils.unscript(qainfo.getQestnSj()));
					qainfo.setQestnCn(CommonUtils.unscript(qainfo.getQestnCn()));
					qainfo.setLastUpdusrId(user.getUniqId());
					qainfoService.updateQainfoQestn(qainfo);
					jsonResult.setSuccess(true);
					//jsonResult.setMessage(egovMessageSource.getMessage("success.common.update"));
					if("SITE".equals(qainfo.getQaSeCode())) {
						jsonResult.setMessage("1:1????????? ?????????????????????.");
					}else if("GOODS".equals(qainfo.getQaSeCode())) {
						jsonResult.setMessage("Q&A??? ??????????????? ?????????????????????.");
					}else {
						jsonResult.setMessage("??????????????? ?????????????????????.");
					}
					
				}
			} catch (Exception e) {
				LOGGER.error("Exception: "+e);
				jsonResult.setMessage(egovMessageSource.getMessage("fail.common.update"));
				jsonResult.setSuccess(false);
			}
		}else{
			jsonResult.setMessage(egovMessageSource.getMessage("fail.common.login"));
		}
		return jsonResult;
	}
	
	/**
	 * ?????? ??????
	 * @param qainfo
	 * @return
	 */
	@RequestMapping(value = "/qainfo/deleteQainfo.json")
	@ResponseBody
	public JsonResult deleteQainfo(QainfoVO qainfo,@RequestParam("qaId") String qaId){
		JsonResult jsonResult = new JsonResult();
		
		Boolean isLogin = EgovUserDetailsHelper.isAuthenticated();
		
		if(isLogin){
			LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();

			try {
				if(StringUtils.isEmpty(qainfo.getQaId()) || StringUtils.isEmpty(qaId)) {
					this.vaildateMessage(egovMessageSource.getMessage("cms.fail.access"), jsonResult); // ????????? ???????????????.
					LOGGER.error("qaId ??? ??????.");
				}else {
					
					
					
					// TODO : ???????????? ?????? ??? ????????? ?????? ??? ?????? ??????
					
					
					
					
					
					
					
					
					
					qainfo.setLastUpdusrId(user.getUniqId());
					qainfoService.deleteQainfo(qainfo);
					jsonResult.setSuccess(true);
					jsonResult.setMessage(egovMessageSource.getMessage("success.common.delete")); //????????? ?????????????????????.
				}
	
			} catch(Exception e) {
				loggerError(LOGGER, e);
				jsonResult.setSuccess(false);
				jsonResult.setMessage(egovMessageSource.getMessage("fail.common.delete")); //????????? ?????????????????????.
			}
		}else{
			jsonResult.setMessage(egovMessageSource.getMessage("fail.common.login"));//????????? ????????? ???????????? ????????????.
		}
		return jsonResult;
	}

	
}
