package modoo.cms.shop.goods.brand.web;

import java.util.ArrayList;
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

import egovframework.com.cmm.EgovMessageSource;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.util.EgovUserDetailsHelper;
import egovframework.com.utl.fcc.service.EgovDateUtil;
import egovframework.rte.psl.dataaccess.util.EgovMap;
import egovframework.rte.ptl.mvc.tags.ui.pagination.PaginationInfo;
import modoo.module.common.service.FileMngUtil;
import modoo.module.common.service.JsonResult;
import modoo.module.common.util.CommonUtils;
import modoo.module.common.web.CommonDefaultController;
import modoo.module.shop.cmpny.service.CmpnyService;
import modoo.module.shop.cmpny.service.CmpnyVO;
import modoo.module.shop.goods.brand.service.GoodsBrandImageService;
import modoo.module.shop.goods.brand.service.GoodsBrandImageVO;
import modoo.module.shop.goods.brand.service.GoodsBrandService;
import modoo.module.shop.goods.brand.service.GoodsBrandVO;
import modoo.module.shop.goods.info.service.GoodsService;

@Controller
public class CmsGoodsBrandController extends CommonDefaultController {

	private static final Logger LOGGER = LoggerFactory.getLogger(CmsGoodsBrandController.class);
	
	@Resource(name="cmpnyService")
	CmpnyService cmpnyService;

	
	private static final Integer DEK_WIDTH = 1100;
	private static final Integer DEK_HEIGHT = 500;
	private static final Integer MOB_WIDTH = 1200;
	private static final Integer MOB_HEIGHT = 600;
	private static final Integer REP_WIDTH = 1920;
	private static final Integer REP_HEIGHT = 415;
	private static final Integer INT_WIDTH = 1400;
	private static final Integer EVT_WEB_HEIGHT = 150;
	private static final Integer EVT_WEB_WIDTH = 1400;
	private static final Integer EVT_MOB_HEIGHT = 231;
	private static final Integer EVT_MOB_WIDTH = 975;


	/** EgovMessageSource */
	@Resource(name = "egovMessageSource")
	protected EgovMessageSource egovMessageSource;
	
	@Resource(name = "goodsBrandService")
	private GoodsBrandService goodsBrandService;
	
	@Resource(name = "goodsBrandImageService")
	private GoodsBrandImageService goodsBrandImageService;
	
	@Resource(name = "fileMngUtil")
	private FileMngUtil fileMngUtil;
	
	/**
	 * ?????? ????????? ??????
	 * @param searchVO
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/decms/embed/shop/goods/goodsBrandManage.do")
	public String goodsBrandManage(@ModelAttribute("searchVO") GoodsBrandVO searchVO, Model model) throws Exception {
		return "modoo/cms/shop/goods/brand/goodsBrandManage";
	}
	
	/**
	 * ????????? ??????
	 * @param searchVO
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/decms/shop/goods/brandManage.do")
	public String BrandManage(@ModelAttribute("searchVO") GoodsBrandVO searchVO,Model model,HttpServletRequest request) throws Exception {
		model.addAttribute("menuId",request.getParameter("menuId"));
		return "modoo/cms/shop/brand/brandManage";
	}
	
	/**
	 * ????????? ??????
	 * @param searchVO
	 * @return
	 */
	@RequestMapping(value = "/decms/shop/goods/goodsBrandList.json")
	@ResponseBody
	public JsonResult goodsBrandList(GoodsBrandVO searchVO) {
		LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
		JsonResult jsonResult = new JsonResult();
		
		try {
			if(!EgovUserDetailsHelper.getAuthorities().contains("ROLE_EMPLOYEE")) {
				if(StringUtils.isNotEmpty(user.getCmpnyId())) {
					searchVO.setSearchCmpnyId(user.getCmpnyId());
				}else {
					jsonResult.setMessage(egovMessageSource.getMessage("cms.fail.not.cmpnyId")); //??????????????? ???????????????.
					jsonResult.setSuccess(false);
					return jsonResult;
				}
			}

			if(StringUtils.isNotEmpty(searchVO.getSearchBgnde())) {
				searchVO.setSearchBgnde(EgovDateUtil.validChkDate(searchVO.getSearchBgnde()));
			}
			if(StringUtils.isNotEmpty(searchVO.getSearchEndde())) {
				searchVO.setSearchEndde(EgovDateUtil.validChkDate(searchVO.getSearchEndde()));
			}
			
			searchVO.setSearchOrderType("DESC");
			
			PaginationInfo paginationInfo = new PaginationInfo();
			searchVO.setPageUnit(propertiesService.getInt("gridPageUnit"));
			this.setPagination(paginationInfo, searchVO);
			
			List<?> resultList = goodsBrandService.selectGoodsBrandList(searchVO);
			System.out.println(resultList.toString());
			jsonResult.put("list", resultList);
			
			int totalRecordCount = goodsBrandService.selectGoodsBrandListCnt(searchVO);
			paginationInfo.setTotalRecordCount(totalRecordCount);
			jsonResult.put("paginationInfo", paginationInfo);
			
			jsonResult.setSuccess(true);
		} catch(Exception e) {
			loggerError(LOGGER, e);
			jsonResult.setSuccess(false);
			jsonResult.setMessage(egovMessageSource.getMessage("fail.common.select")); //????????? ?????????????????????.
		}
		
		return jsonResult;
	}
	
	/**
	 * ???????????????/?????? ???
	 * @param searchVO
	 * @param model
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/decms/shop/goods/writeBrand.do")
	public String writeBrand(@ModelAttribute("searchVO") GoodsBrandVO searchVO, Model model,
			HttpServletRequest request) throws Exception {
		LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
		
		if(!StringUtils.isEmpty(searchVO.getBrandId())){ // ??????
			//searchVO.setBrandId(request.getParameter("brandId"));
			GoodsBrandVO brand = goodsBrandService.selectGoodsBrand(searchVO);
			model.addAttribute("brand",brand);
		}else { // ????????????
			GoodsBrandVO brand = new GoodsBrandVO();
			if(!EgovUserDetailsHelper.getAuthorities().contains("ROLE_EMPLOYEE")) {
				if(StringUtils.isEmpty(user.getCmpnyId())) {
					return "modoo/common/error/error";
				}
				//
				CmpnyVO cmpny = new CmpnyVO();
				cmpny.setCmpnyId(user.getCmpnyId());
				cmpny = cmpnyService.selectCmpny(cmpny);
				
				brand.setCmpnyNm(cmpny.getCmpnyNm());
				brand.setCmpnyId(user.getCmpnyId());
			}
			model.addAttribute("brand",brand);
		}
		model.addAttribute("menuId",request.getParameter("menuId"));
		
		return "modoo/cms/shop/brand/brandForm";
	}
	/**
	 * ????????? ??????
	 * @param goodsBrand
	 * @param bindingResult
	 * @return
	 */
	@RequestMapping(value = "/decms/shop/goods/saveGoodsBrand.json", method = RequestMethod.POST)
	@ResponseBody
	public JsonResult writeGoodsBrand(final MultipartHttpServletRequest multiRequest,
			@Valid GoodsBrandVO goodsBrand, BindingResult bindingResult,
			@RequestParam(value = "isForm", required = false) String isForm,
			@RequestParam(value = "menuId") String menuId) {
		LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
		JsonResult jsonResult = new JsonResult();
		
		try {

			if(!EgovUserDetailsHelper.getAuthorities().contains("ROLE_EMPLOYEE")) {
				if(StringUtils.isNotEmpty(user.getCmpnyId())) {
					goodsBrand.setCmpnyId(user.getCmpnyId());
				}else {
					jsonResult.setMessage(egovMessageSource.getMessage("cms.fail.not.cmpnyId")); //??????????????? ???????????????.
					jsonResult.setSuccess(false);
					return jsonResult;
				}
			}
			
			if(!this.isHasErrorsJson(bindingResult, jsonResult, "<br/>")) {
				goodsBrand.setBrandIntCn(CommonUtils.unscript(goodsBrand.getBrandIntCn()));
				goodsBrand.setBrandIntLink(CommonUtils.unscript(goodsBrand.getBrandIntLink()));
				
				if(goodsBrand.getSvcAdres()== null  || goodsBrand.getSvcAdres().length() == 0) {
					jsonResult.setMessage("??????/?????? ????????? ?????? ???????????????.");
					jsonResult.setSuccess(false);
					return jsonResult;
				} else if(goodsBrand.getSvcHdryNm()== null  || goodsBrand.getSvcHdryNm().length() == 0) {
					jsonResult.setMessage("??????/?????? ??????????????? ?????? ???????????????.");
					jsonResult.setSuccess(false);
					return jsonResult;
				} else if(goodsBrand.getRtngudDlvyPc() == null) {
					jsonResult.setMessage("?????? ???????????? ?????? ???????????????.");
					jsonResult.setSuccess(false);
					return jsonResult;
				} else if(goodsBrand.getExchngDlvyPc() == null) {
					jsonResult.setMessage("?????? ???????????? ?????? ???????????????.");
					jsonResult.setSuccess(false);
					return jsonResult;
				}
				
				final MultipartFile atchFile = multiRequest.getFile("atchLogoFile");
				if(atchFile != null && !atchFile.isEmpty()) {
					EgovMap fmap = fileMngUtil.parseImageContentFile(atchFile, null, null);
					String brandImagePath = (String) fmap.get("orignFileUrl");
					String brandImageThumbPath = (String) fmap.get("thumbUrl");
					goodsBrand.setBrandImagePath(brandImagePath);
					goodsBrand.setBrandImageThumbPath(brandImageThumbPath);
				}
				
				// ??????????????? (?????????)
				final MultipartFile repFile = multiRequest.getFile("atchRepFile");
				if(repFile != null && !repFile.isEmpty()) {
					List<GoodsBrandImageVO> repBrandImageList = new ArrayList<GoodsBrandImageVO>();
					EgovMap fmap = fileMngUtil.parseImageContentFile(repFile, REP_WIDTH, REP_HEIGHT, null, null);
					String brandImagePath = (String) fmap.get("orignFileUrl");
					String brandImageThumbPath = (String) fmap.get("thumbUrl");
					GoodsBrandImageVO brandImage = new GoodsBrandImageVO(); 
					brandImage.setBrandImageSeCode("REP"); // ???????????????
					brandImage.setBrandImagePath(brandImagePath);
					brandImage.setBrandImageThumbPath(brandImageThumbPath);
					repBrandImageList.add(brandImage);
					goodsBrand.setRepBrandImageList(repBrandImageList);
				}
				
				// ??????????????? (?????????)
				final MultipartFile repMobFile = multiRequest.getFile("atchRepMobFile");
				if(repMobFile != null && !repMobFile.isEmpty()) {
					List<GoodsBrandImageVO> repMobBrandImageList = new ArrayList<GoodsBrandImageVO>();
					EgovMap fmap = fileMngUtil.parseImageContentFile(repMobFile, MOB_WIDTH, MOB_HEIGHT, null, null);
					String brandImagePath = (String) fmap.get("orignFileUrl");
					String brandImageThumbPath = (String) fmap.get("thumbUrl");
					
					GoodsBrandImageVO brandImage = new GoodsBrandImageVO(); 
					brandImage.setBrandImageSeCode("REPMOB"); // ????????????????????????
					brandImage.setBrandImagePath(brandImagePath);
					brandImage.setBrandImageThumbPath(brandImageThumbPath);
					repMobBrandImageList.add(brandImage);
					goodsBrand.setRepMobBrandImageList(repMobBrandImageList);
				}

				// ????????? ?????? (?????????)
				final MultipartFile evtFile = multiRequest.getFile("atchEvtFile");
				if(evtFile != null && !evtFile.isEmpty()) {
					List<GoodsBrandImageVO> evtBrandImageList = new ArrayList<GoodsBrandImageVO>();
					EgovMap fmap = fileMngUtil.parseImageContentFile(evtFile, null, null, null, null);
					String brandImagePath = (String) fmap.get("orignFileUrl");
					String brandImageThumbPath = (String) fmap.get("thumbUrl");
					GoodsBrandImageVO brandImage = new GoodsBrandImageVO(); 
					brandImage.setBrandImageSeCode("EVT"); // ???????????????
					brandImage.setBrandImagePath(brandImagePath);
					brandImage.setBrandImageThumbPath(brandImageThumbPath);
					evtBrandImageList.add(brandImage);
					goodsBrand.setEvtBrandImageList(evtBrandImageList);
				}
				
				// ????????? ?????? (?????????)
				final MultipartFile evtMobFile = multiRequest.getFile("atchEvtMobFile");
				if(evtMobFile != null && !evtMobFile.isEmpty()) {
					List<GoodsBrandImageVO> evtMobBrandImageList = new ArrayList<GoodsBrandImageVO>();
					EgovMap fmap = fileMngUtil.parseImageContentFile(evtMobFile, null, null, null, null);
					String brandImagePath = (String) fmap.get("orignFileUrl");
					String brandImageThumbPath = (String) fmap.get("thumbUrl");
					
					GoodsBrandImageVO brandImage = new GoodsBrandImageVO(); 
					brandImage.setBrandImageSeCode("EVTMOB"); // ????????????????????????
					brandImage.setBrandImagePath(brandImagePath);
					brandImage.setBrandImageThumbPath(brandImageThumbPath);
					evtMobBrandImageList.add(brandImage);
					goodsBrand.setEvtMobBrandImageList(evtMobBrandImageList);
				}
				
				/*   // ???????????? ????????? (?????? ?????? ????????? ??????)
				final List<MultipartFile> repFileList = multiRequest.getFiles("atchRepFile");
				if(!repFileList.isEmpty()) {
					List<GoodsBrandImageVO> repBrandImageList = new ArrayList<GoodsBrandImageVO>();
					for(MultipartFile mFile : repFileList) {
						GoodsBrandImageVO brandImage = new GoodsBrandImageVO(); 
						EgovMap fmap = fileMngUtil.parseImageContentFile(mFile, REP_WIDTH, REP_HEIGHT, null, null);
						String brandImagePath = (String) fmap.get("orignFileUrl");
						String brandImageThumbPath = (String) fmap.get("thumbUrl");
						brandImage.setBrandImageSeCode("REP"); // ???????????????
						brandImage.setBrandImagePath(brandImagePath);
						brandImage.setBrandImageThumbPath(brandImageThumbPath);
						repBrandImageList.add(brandImage);
					}
					goodsBrand.setRepBrandImageList(repBrandImageList);
				}
				*/
				
				// ???????????????
				final MultipartFile intFile = multiRequest.getFile("atchIntFile");
				if(intFile != null && !intFile.isEmpty()) {
					List<GoodsBrandImageVO> intBrandImageList = new ArrayList<GoodsBrandImageVO>();
					EgovMap fmap = fileMngUtil.parseImageContentFile(intFile, INT_WIDTH, null, null, null);
					String brandImagePath = (String) fmap.get("orignFileUrl");
					String brandImageThumbPath = (String) fmap.get("thumbUrl");
					
					GoodsBrandImageVO brandImage = new GoodsBrandImageVO(); 
					brandImage.setBrandImageSeCode("INT"); // ?????? ?????????
					brandImage.setBrandImagePath(brandImagePath);
					brandImage.setBrandImageThumbPath(brandImageThumbPath);
					intBrandImageList.add(brandImage);
					
					goodsBrand.setIntBrandImageList(intBrandImageList);
				}
				/*   // ???????????? ????????? (?????? ?????? ????????? ??????)
				final List<MultipartFile> intFileList = multiRequest.getFiles("atchIntFile");
				if(!intFileList.isEmpty()) {
					List<GoodsBrandImageVO> intBrandImageList = new ArrayList<GoodsBrandImageVO>();
					for(MultipartFile mFile : intFileList) {
						GoodsBrandImageVO brandImage = new GoodsBrandImageVO(); 
						EgovMap fmap = fileMngUtil.parseImageContentFile(mFile, INT_WIDTH, null, null, null);
						String brandImagePath = (String) fmap.get("orignFileUrl");
						String brandImageThumbPath = (String) fmap.get("thumbUrl");
						brandImage.setBrandImageSeCode("INT"); // ?????? ?????????
						brandImage.setBrandImagePath(brandImagePath);
						brandImage.setBrandImageThumbPath(brandImageThumbPath);
						intBrandImageList.add(brandImage);
					}
					goodsBrand.setIntBrandImageList(intBrandImageList);
				}
				*/
				
				if(StringUtils.isEmpty(goodsBrand.getBrandId())) {
					goodsBrandService.insertGoodsBrand(goodsBrand);
				}else {
					goodsBrandService.updateGoodsBrand(goodsBrand);
				}
				
				//????????? ?????? ?????? ??????
				goodsBrandService.reloadGoodsBrandGroupList();
				
				
				jsonResult.setSuccess(true);
				if(("Y").equals(isForm)){
					jsonResult.setRedirectUrl("/decms/shop/goods/brandManage.do?menuId="+menuId);
				}
			}

		} catch(Exception e) {
			loggerError(LOGGER, e);
			jsonResult.setSuccess(false);
			jsonResult.setMessage(egovMessageSource.getMessage("fail.common.insert")); //????????? ?????????????????????.
		}
		
		return jsonResult;
		
	}
	
	/**
	 * ????????? ??????
	 * @param goodsBrand
	 * @return
	 */
	@RequestMapping(value = "/decms/shop/goods/goodsBrand.json")
	@ResponseBody
	public JsonResult goodsBrand(GoodsBrandVO searchVO) {
		JsonResult jsonResult = new JsonResult();
		try {
			GoodsBrandVO goodsBrand = goodsBrandService.selectGoodsBrand(searchVO);
			
			jsonResult.put("goodsBrand", goodsBrand);
			jsonResult.setSuccess(true);
		} catch(Exception e) {
			loggerError(LOGGER, e);
			jsonResult.setSuccess(false);
			jsonResult.setMessage(egovMessageSource.getMessage("fail.common.select")); //????????? ?????????????????????.
		}
		
		return jsonResult;
	}
	
	/**
	 * ????????? ??????
	 * @param goodsBrand
	 * @return
	 */
	@RequestMapping(value = "/decms/shop/goods/deleteGoodsBrand.json")
	@ResponseBody
	public JsonResult deleteGoodsBrand(GoodsBrandVO goodsBrand) {
		LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
		JsonResult jsonResult = new JsonResult();

		try {
			if(StringUtils.isEmpty(goodsBrand.getBrandId())) {
				this.vaildateMessage(egovMessageSource.getMessage("cms.fail.access"), jsonResult); // ????????? ???????????????.
				LOGGER.error("brandId ??? ??????.");
			}else {
				if(!EgovUserDetailsHelper.getAuthorities().contains("ROLE_EMPLOYEE")) {
					if(StringUtils.isEmpty(user.getCmpnyId())) {
						jsonResult.setMessage(egovMessageSource.getMessage("cms.fail.not.cmpnyId")); //??????????????? ???????????????.
						jsonResult.setSuccess(false);
						return jsonResult;
					}

					GoodsBrandVO checkVO = goodsBrandService.selectGoodsBrand(goodsBrand);
					if(!user.getCmpnyId().equals(checkVO.getCmpnyId())) { //???????????? ?????? ?????????
						jsonResult.setMessage(egovMessageSource.getMessage("cms.fail.accessDenied")); //?????? ????????? ????????????.
						jsonResult.setSuccess(false);
						return jsonResult;
					}
				}
				
				goodsBrandService.deleteGoodsBrand(goodsBrand);
				
				//????????? ?????? ?????? ??????
				goodsBrandService.reloadGoodsBrandGroupList();
				
				jsonResult.setSuccess(true);
				jsonResult.setMessage(egovMessageSource.getMessage("success.common.delete")); //??????????????? ?????????????????????.
			}
		
		} catch(Exception e) {
			loggerError(LOGGER, e);
			jsonResult.setSuccess(false);
			jsonResult.setMessage(egovMessageSource.getMessage("fail.common.delete")); //????????? ?????????????????????.
		}
		
		return jsonResult;
	}
	
	/**
	 * ?????????????????? ??????
	 * @param brandImage
	 * @return
	 */
	@RequestMapping(value = "/decms/shop/goods/deleteGoodsBrandImage.json")
	@ResponseBody
	public JsonResult deleteGoodsBrandImage(GoodsBrandImageVO brandImage) {
		LoginVO user = (LoginVO)EgovUserDetailsHelper.getAuthenticatedUser();
		JsonResult jsonResult = new JsonResult();
		try {
			if(brandImage.getBrandImageNo() == null) {
				this.vaildateMessage(egovMessageSource.getMessage("cms.fail.access"), jsonResult); // ????????? ???????????????.
				LOGGER.error("brandImageNo ??? ??????.");
			}else {
				if(!EgovUserDetailsHelper.getAuthorities().contains("ROLE_EMPLOYEE")) {
					if(StringUtils.isEmpty(user.getCmpnyId())) {
						jsonResult.setMessage(egovMessageSource.getMessage("cms.fail.not.cmpnyId")); //??????????????? ???????????????.
						jsonResult.setSuccess(false);
						return jsonResult;
					}
					
					String cmpnyId = goodsBrandImageService.selectCheckGoodsBrandCmpnyId(brandImage);
					if(!cmpnyId.equals(user.getCmpnyId())) { //???????????? ?????? ?????????
						jsonResult.setMessage(egovMessageSource.getMessage("cms.fail.accessDenied")); //?????? ????????? ????????????.
						jsonResult.setSuccess(false);
						return jsonResult;
					}
				}
				
				goodsBrandImageService.deleteGoodsBrandImage(brandImage);
				
				jsonResult.setSuccess(true);
				jsonResult.setMessage(egovMessageSource.getMessage("success.common.delete")); //??????????????? ?????????????????????.
			}
			
		} catch(Exception e) {
			loggerError(LOGGER, e);
			jsonResult.setSuccess(false);
			jsonResult.setMessage(egovMessageSource.getMessage("fail.common.delete")); //????????? ?????????????????????.
		}
		
		return jsonResult;
	}

}
