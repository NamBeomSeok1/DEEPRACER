package modoo.module.shop.goods.info.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;
import egovframework.rte.fdl.idgnr.EgovIdGnrService;
import egovframework.rte.psl.dataaccess.util.EgovMap;
import modoo.module.shop.cmpny.service.PrtnrCmpnyVO;
import modoo.module.shop.cmpny.service.impl.PrtnrCmpnyMapper;
import modoo.module.shop.goods.image.service.GoodsImageVO;
import modoo.module.shop.goods.image.service.impl.GoodsImageMapper;
import modoo.module.shop.goods.info.service.GoodsCouponVO;
import modoo.module.shop.goods.info.service.GoodsItemVO;
import modoo.module.shop.goods.info.service.GoodsService;
import modoo.module.shop.goods.info.service.GoodsVO;
import modoo.module.shop.goods.keyword.service.GoodsKeywordVO;
import modoo.module.shop.goods.keyword.service.impl.GoodsKeywordMapper;
import modoo.module.shop.goods.order.service.OrderVO;
import modoo.module.shop.goods.recomend.service.GoodsRecomendVO;
import modoo.module.shop.goods.recomend.service.impl.GoodsRecomendMapper;

@Service("goodsService")
public class GoodsServiceImpl extends EgovAbstractServiceImpl implements GoodsService {
	
	@Resource(name = "goodsMapper")
	private GoodsMapper goodsMapper;
	
	@Resource(name = "goodsItemMapper")
	private GoodsItemMapper goodsItemMapper;
	
	@Resource(name = "goodsKeywordMapper")
	private GoodsKeywordMapper goodsKeywordMapper;
	
	@Resource(name = "goodsImageMapper")
	private GoodsImageMapper goodsImageMapper;
	
	@Resource(name = "goodsRecomendMapper")
	private GoodsRecomendMapper goodsRecomendMapper;
	
	@Resource(name = "prtnrCmpnyMapper")
	private PrtnrCmpnyMapper prtnrCmpnyMapper;
	
	@Resource(name = "goodsIdGnrService")
	private EgovIdGnrService goodsIdGnrService;
	
	@Resource(name = "goodsItemIdGnrService")
	private EgovIdGnrService goodsItemIdGnrService;
	
	@Resource(name = "goodsKeywordIdGnrService")
	private EgovIdGnrService goodsKeywordIdGnrService;
	
	@Resource(name = "goodsImageIdGnrService")
	private EgovIdGnrService goodsImageIdGnrService;
	
	@Resource(name = "goodsRecomendIdGnrService")
	private EgovIdGnrService goodsRecomendIdGnrService;
	
	@Resource(name = "goodsCouponMapper")
	private GoodsCouponMapper goodsCouponMapper;

	/**
	 * ?????? ??????
	 */
	@Override
	public List<?> selectGoodsList(GoodsVO searchVO) throws Exception {
		return goodsMapper.selectGoodsList(searchVO);
	}

	/**
	 * ?????? ?????? ?????????
	 */
	@Override
	public int selectGoodsListCnt(GoodsVO searchVO) throws Exception {
		return goodsMapper.selectGoodsListCnt(searchVO);
	}

	/**
	 * ?????? ??????
	 */
	@Override
	public void insertGoods(GoodsVO goods) throws Exception {
		String goodsId = goodsIdGnrService.getNextStringId();
		goods.setGoodsId(goodsId);
		
		//???????????? ???????????? ?????? ????????? ??????
		if(goods.getGoodsPc().equals(java.math.BigDecimal.ZERO)) {
			goods.setGoodsFeeRate(java.math.BigDecimal.ZERO);
			goods.setGoodsSplpc(java.math.BigDecimal.ZERO);
		}else {
			java.math.BigDecimal goodsSplpc = new java.math.BigDecimal(0);
			goodsSplpc = goods.getGoodsPc().subtract(goods.getGoodsPc().multiply(goods.getGoodsFeeRate().divide(new java.math.BigDecimal(100))));
			goods.setGoodsSplpc(goodsSplpc);
		}
		
		if("N".equals(goods.getfOptnUseAt())) { //???????????????
			goods.setFrstOptnEssntlAt("N"); //??????????????? ????????????
		}
		if(goods.getFrstOptnEssntlAt() == null) {
			goods.setFrstOptnEssntlAt("N");
		}
		
		goodsMapper.insertGoods(goods);
		
		if(!"WEEK".equals(goods.getSbscrptCycleSeCode())) {
			goods.setSbscrptWeekCycle(null);
			goods.setSbscrptDlvyWd(null);
			goods.setSbscrptMinUseWeek(null);
		}else if(!"MONTH".equals(goods.getSbscrptCycleSeCode())) {
			goods.setSbscrptMtCycle(null);
			goods.setSbscrptMinUseMt(null);
			goods.setSbscrptDlvyDay(null);
		}
		
		if("Y".equals(goods.getOptnUseAt())) {
			insertOrUpdate(goods, goods.getdOptnUseAt(), "D", goods.getdGitemList());
			insertOrUpdate(goods, goods.getaOptnUseAt(), "A", goods.getaGitemList());
			insertOrUpdate(goods, goods.getfOptnUseAt(), "F", goods.getfGitemList());
			insertOrUpdate(goods, goods.getqOptnUseAt(), "Q", goods.getqGitemList());
		}
		
		//????????? ?????? ??????
		if(goods.getGoodsKeywordList() != null) {
			for(GoodsKeywordVO goodsKeyword: goods.getGoodsKeywordList()) {
				java.math.BigDecimal no = goodsKeywordIdGnrService.getNextBigDecimalId();
				goodsKeyword.setGoodsKeywordNo(no);
				goodsKeyword.setGoodsId(goods.getGoodsId());
				goodsKeywordMapper.insertGoodsKeyword(goodsKeyword);
			}
		}
		
		//?????? ??????????????? ??????
		saveGoodsImage(goods.getGoodsId(), goods.getGdcImageList());
		saveGoodsImage(goods.getGoodsId(), goods.getGoodsImageList());
		//????????? ?????????
		saveGoodsImage(goods.getGoodsId(), goods.getEvtImageList());
		//saveGoodsImage(goods.getGoodsId(), goods.getPcImageList());
		//saveGoodsImage(goods.getGoodsId(), goods.getMobImageList());
		//saveGoodsImage(goods.getGoodsId(), goods.getBanImageList());

		//????????????
		if(goods.getGoodsRecomendList() != null) {
			for(GoodsRecomendVO recomend : goods.getGoodsRecomendList()) {
				recomend.setGoodsId( goods.getGoodsId() );
				java.math.BigDecimal no = goodsRecomendIdGnrService.getNextBigDecimalId();
				recomend.setGoodsRecomendNo(no);
				if(recomend.getRecomendGoodsSn() == null) recomend.setRecomendGoodsSn(0);
				goodsRecomendMapper.insertGoodsRecomend(recomend);
			}
		}
		
		//??????????????????
		if("CPN".equals(goods.getGoodsKndCode())) {
			String uploadGroupId =  goods.getFrstRegisterId(); //(StringUtils.isEmpty(goods.getCmpnyId())?"SYSTEM":goods.getCmpnyId()) + goods.getFrstRegisterId();
			GoodsCouponVO goodsCoupon = new GoodsCouponVO();
			goodsCoupon.setUploadGroupId(uploadGroupId);
			goodsCoupon.setGoodsId(goods.getGoodsId());
			goodsCouponMapper.insertTmpCouponToGoodsCoupon(goodsCoupon);
		}
	}
	
	private void saveGoodsImage(String goodsId, List<GoodsImageVO> imageList) throws Exception {
		if(imageList != null) {
			for(GoodsImageVO img : imageList) {
				img.setGoodsId(goodsId);
				if(img.getGoodsImageNo() == null) {
					java.math.BigDecimal no = goodsImageIdGnrService.getNextBigDecimalId();
					img.setGoodsImageNo(no);
					if(img.getGoodsImageSn() == null) img.setGoodsImageSn(0);
					goodsImageMapper.insertGoodsImage(img);
				}else {
					goodsImageMapper.updateGoodsImage(img);
				}
			}
		}
	}

	/**
	 * ?????? ??????
	 */
	@Override
	public GoodsVO selectGoods(GoodsVO goods) throws Exception {
		
		GoodsVO vo = goodsMapper.selectGoods(goods);
		
		if(vo != null) {
			//?????????????????????
			PrtnrCmpnyVO prtnrCmpny = new PrtnrCmpnyVO();
			prtnrCmpny.setCmpnyId(vo.getCmpnyId());
			List<PrtnrCmpnyVO> prtnrCmpnyList = prtnrCmpnyMapper.selectPrtnrCmpnyList(prtnrCmpny);
			vo.setPrtnrCmpnyList(prtnrCmpnyList);
			//prtnrCmpny.setCmpnyId(vo.getPcmapngId());


			//???????????? ??????
			GoodsItemVO goodsItem = new GoodsItemVO();
			goodsItem.setGoodsId(goods.getGoodsId());
			
			List<GoodsItemVO> goodsItemList = goodsItemMapper.selectGoodsItemList(goodsItem);
			List<GoodsItemVO> dGitemList = new ArrayList<GoodsItemVO>();
			List<GoodsItemVO> aGitemList = new ArrayList<GoodsItemVO>();
			List<GoodsItemVO> fGitemList = new ArrayList<GoodsItemVO>();
			List<GoodsItemVO> qGitemList = new ArrayList<GoodsItemVO>();
			for(GoodsItemVO item : goodsItemList) {
				if("D".equals(item.getGitemSeCode())) {
					dGitemList.add(item);
				}else if("A".equals(item.getGitemSeCode())) {
					aGitemList.add(item);
				}else if("F".equals(item.getGitemSeCode())) {
					fGitemList.add(item);
				}else if("Q".equals(item.getGitemSeCode())) {
					qGitemList.add(item);
				}
			}
			vo.setdGitemList(dGitemList);
			vo.setaGitemList(aGitemList);
			vo.setfGitemList(fGitemList);
			vo.setqGitemList(qGitemList);
			
			//vo.setGoodsItemList(goodsItemList);
			
			//????????? ??????
			List<GoodsKeywordVO> goodsKeywordList = goodsKeywordMapper.selectGoodsKeywordList(goods);
			vo.setGoodsKeywordList(goodsKeywordList);
			
			GoodsImageVO goodsImage = new GoodsImageVO();
			goodsImage.setGoodsId(goods.getGoodsId());
			goodsImage.setGoodsImageSeCode("GDC"); // ?????????????????????
			List<GoodsImageVO> gdcImageList = goodsImageMapper.selectGoodsImageList(goodsImage);
			vo.setGdcImageList(gdcImageList);

			goodsImage.setGoodsImageSeCode("GNR"); // ?????? ????????? 
			List<GoodsImageVO> goodsImageList = goodsImageMapper.selectGoodsImageList(goodsImage);
			vo.setGoodsImageList(goodsImageList);
			
			goodsImage.setGoodsImageSeCode("EVT"); // ????????? ????????? 
			List<GoodsImageVO> evtImgList = goodsImageMapper.selectGoodsImageList(goodsImage);
			vo.setEvtImageList(evtImgList);
			
			//?????? ??? ?????? ??????
			if(StringUtils.isNotEmpty(vo.getSbscrptWeekCycle())) {
				String[] sbscrptWeekCycleArray = vo.getSbscrptWeekCycle().split(",");
				vo.setSbscrptWeekCycleList(Arrays.asList(sbscrptWeekCycleArray));
			}
			//?????? ??? ???????????? ?????? 
			if(StringUtils.isNotEmpty(vo.getSbscrptDlvyWd())) {
				String[] sbscrptDlvyWdArray = vo.getSbscrptDlvyWd().split(",");
				vo.setSbscrptDlvyWdList(Arrays.asList(sbscrptDlvyWdArray));
			}
			//?????? ??? ?????? ??????
			if(StringUtils.isNotEmpty(vo.getSbscrptMtCycle())) {
				String[] sbscrptMtCycleArray = vo.getSbscrptMtCycle().split(",");
				vo.setSbscrptMtCycleList(Arrays.asList(sbscrptMtCycleArray));
			}
			
			// ?????? ????????????(?????????????????? -> ????????? ????????????)
			/*GoodsRecomendVO goodsRecomend = new GoodsRecomendVO();
			goodsRecomend.setGoodsId(goods.getGoodsId());*/
			/*List<GoodsRecomendVO> goodsRecomendList = goodsRecomendMapper.selectGoodsRecomendList(goodsRecomend);*/
			
			if(vo.getBrandId()!=null){
				GoodsVO brandGoods = new GoodsVO();
				brandGoods.setSearchGoodsBrandId(vo.getBrandId());
				brandGoods.setSearchPrtnrId(vo.getPrtnrId());
				List<?> goodsRecomendList = goodsMapper.selectGoodsList(brandGoods);
				vo.setBrandGoodsRecomendList(goodsRecomendList);
			}
		}
		
		return vo;
	}

	/**
	 * ?????? ?????? ??? ?????? ????????? ?????? ??????
	 * @param goods
	 * @param gitemType
	 * @param gitemList
	 * @throws Exception
	 */
	private void insertOrUpdate(GoodsVO goods, String useAt, String gitemType, List<GoodsItemVO> gitemList) throws Exception {
		if("Y".equals(useAt)) {
			// ????????????
			for(GoodsItemVO gitem: gitemList) {
				if(StringUtils.isNotEmpty(gitem.getGitemNm())) {
					//?????? ?????? ?????? 
					if(StringUtils.isEmpty(gitem.getGitemSttusCode())) gitem.setGitemSttusCode("T"); //????????????
					gitem.setGitemSeCode(gitemType); //
					if(gitem.getGitemCo() == null) gitem.setGitemCo(0); //??????
					gitem.setGoodsId(goods.getGoodsId());

					if(StringUtils.isEmpty(gitem.getGitemId())) {
						String gitemId = goodsItemIdGnrService.getNextStringId();
						gitem.setGitemId(gitemId);
						goodsItemMapper.insertGoodsItem(gitem);
					}else {
						goodsItemMapper.updateGoodsItem(gitem);
					}
				}
			}
		}else {
			GoodsItemVO gitem = new GoodsItemVO();
			gitem.setGoodsId(goods.getGoodsId());
			gitem.setGitemSeCode(gitemType);
			goodsItemMapper.deleteGoodsItemList(gitem);
		}
	}
	/**
	 * ?????? ??????
	 */
	@Override
	public void updateGoods(GoodsVO goods) throws Exception {
		
		if(!"WEEK".equals(goods.getSbscrptCycleSeCode())) {
			goods.setSbscrptWeekCycle(null);
			goods.setSbscrptDlvyWd(null);
			goods.setSbscrptMinUseWeek(null);
		}else if(!"MONTH".equals(goods.getSbscrptCycleSeCode())) {
			goods.setSbscrptMtCycle(null);
			goods.setSbscrptMinUseMt(null);
			goods.setSbscrptDlvyDay(null);
		}
		
		if("Y".equals(goods.getOptnUseAt())) {
			insertOrUpdate(goods, goods.getdOptnUseAt(), "D", goods.getdGitemList());
			insertOrUpdate(goods, goods.getaOptnUseAt(), "A", goods.getaGitemList());
			insertOrUpdate(goods, goods.getfOptnUseAt(), "F", goods.getfGitemList());
			insertOrUpdate(goods, goods.getqOptnUseAt(), "Q", goods.getqGitemList());
		}else {
			// ?????? ????????????
			GoodsItemVO gitem = new GoodsItemVO();
			gitem.setGoodsId(goods.getGoodsId());
			goodsItemMapper.deleteGoodsItemList(gitem);
		}
		
		if("N".equals(goods.getfOptnUseAt())) { //???????????????
			goods.setFrstOptnEssntlAt("N"); //??????????????? ????????????
		}
		if(goods.getFrstOptnEssntlAt() == null) {
			goods.setFrstOptnEssntlAt("N");
		}
		
		//????????? ?????? ??????
		if(goods.getGoodsKeywordList() != null) {
			for(GoodsKeywordVO goodsKeyword: goods.getGoodsKeywordList()) {
				goodsKeyword.setGoodsId(goods.getGoodsId());
				if(goodsKeyword.getGoodsKeywordNo() == null) {
					java.math.BigDecimal no = goodsKeywordIdGnrService.getNextBigDecimalId();
					goodsKeyword.setGoodsKeywordNo(no);
					goodsKeywordMapper.insertGoodsKeyword(goodsKeyword);
				}else {
					goodsKeywordMapper.updateGoodsKeyword(goodsKeyword);
				}
			}
		}
		
		//?????? ??????????????? ??????
		saveGoodsImage(goods.getGoodsId(), goods.getGdcImageList());
		//?????? ?????????
		saveGoodsImage(goods.getGoodsId(), goods.getGoodsImageList());
		//????????? ?????????
		saveGoodsImage(goods.getGoodsId(), goods.getEvtImageList());
		
		//????????????
		if(goods.getGoodsRecomendList() != null) {
			for(GoodsRecomendVO recomend : goods.getGoodsRecomendList()) {
				recomend.setGoodsId( goods.getGoodsId() );
				if(recomend.getGoodsRecomendNo() == null) {
					java.math.BigDecimal no = goodsRecomendIdGnrService.getNextBigDecimalId();
					recomend.setGoodsRecomendNo(no);
					if(recomend.getRecomendGoodsSn() == null) recomend.setRecomendGoodsSn(0);
					goodsRecomendMapper.insertGoodsRecomend(recomend);
				}else {
					goodsRecomendMapper.updateGoodsRecomend(recomend);
				}
			}
		}
		
		//???????????? ???????????? ?????? ????????? ??????
		if(goods.getGoodsPc().equals(java.math.BigDecimal.ZERO)) {
			goods.setGoodsFeeRate(java.math.BigDecimal.ZERO);
			goods.setGoodsSplpc(java.math.BigDecimal.ZERO);
		}else {
			java.math.BigDecimal goodsSplpc = new java.math.BigDecimal(0); //???????????? 0 ??????
			if(goods.getGoodsFeeRate() == null || goods.getGoodsFeeRate().equals(java.math.BigDecimal.ZERO)) {
				goods.setGoodsFeeRate(java.math.BigDecimal.ZERO); 
				goodsSplpc = goods.getGoodsPc();
			}else {
				goodsSplpc = goods.getGoodsPc().subtract(goods.getGoodsPc().multiply(goods.getGoodsFeeRate().divide(new java.math.BigDecimal(100))));
			}
			goods.setGoodsSplpc(goodsSplpc);
		}
		
		goodsMapper.updateGoods(goods);
	}
	
	/**
	 * ?????? ????????? ??????
	 */
	@Override
	public void updateGoodsRdcnt(GoodsVO goods) throws Exception {
		goodsMapper.updateGoodsRdcnt(goods);
	}

	/**
	 * ?????? ??????
	 */
	@Override
	public void deleteGoods(GoodsVO goods) throws Exception {
		goodsMapper.deleteGoods(goods);
	}

	/**
	 * ?????? ?????? ?????????
	 */
	@Override
	public EgovMap selectGoodsSttusCnt(GoodsVO searchVO) throws Exception {
		return goodsMapper.selectGoodsSttusCnt(searchVO);
	}

	/**
	 * ????????? ?????? ??????
	 */
	@Override
	public List<?> selectBestGoodsList(GoodsVO searchVO) throws Exception {
		return goodsMapper.selectBestGoodsList(searchVO);
	}

	/**
	 * ????????? ?????? ?????? ?????????
	 */
	@Override
	public int selectBestGoodsListCnt(GoodsVO searchVO) throws Exception {
		return goodsMapper.selectBestGoodsListCnt(searchVO);
	}

	/**
	 * ?????????????????? ????????? ????????????
	 */
	@Override
	public int selectPrtnrCmpnyGoodsListCnt(GoodsVO searchVO) throws Exception {
		return goodsMapper.selectPrtnrCmpnyGoodsListCnt(searchVO);
	}
	/**
	 * ?????? ?????? ?????? ?????? ??????
	 */
	@Override
	public void updateGoodsRegistSttus(GoodsVO goods) {
		goodsMapper.updateGoodsRegistSttus(goods);
	}
	
	/**
	 * ?????? ?????? ??????
	 */
	@Override
	public List<?> selectMainGoodsList(GoodsVO searchVO) throws Exception {
		return goodsMapper.selectMainGoodsList(searchVO);
	}
	
	/**
	 * ?????? ?????? ?????? ?????????
	 */
	@Override
	public int selectMainGoodsListCnt(GoodsVO searchVO) throws Exception {
		return goodsMapper.selectMainGoodsListCnt(searchVO);
	}
}
