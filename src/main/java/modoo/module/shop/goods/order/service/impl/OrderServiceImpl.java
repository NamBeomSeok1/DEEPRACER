package modoo.module.shop.goods.order.service.impl;

import java.math.BigDecimal;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import com.inicis.std.util.SignatureUtil;
import egovframework.com.cmm.LoginVO;
import egovframework.com.cmm.service.EgovProperties;
import egovframework.com.cmm.util.EgovUserDetailsHelper;
import modoo.module.common.service.JsonResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;


import egovframework.com.utl.fcc.service.EgovDateUtil;
import egovframework.rte.fdl.cmmn.EgovAbstractServiceImpl;
import egovframework.rte.fdl.idgnr.EgovIdGnrService;
import egovframework.rte.psl.dataaccess.util.EgovMap;
import modoo.module.biling.service.Biling;
import modoo.module.biling.service.EzwelFunc;
import modoo.module.common.util.CommonUtils;
import modoo.module.common.util.NumberUtil;
import modoo.module.qainfo.service.QainfoVO;
import modoo.module.qainfo.service.impl.QainfoMapper;
import modoo.module.shop.goods.cart.service.CartItem;
import modoo.module.shop.goods.cart.service.CartVO;
import modoo.module.shop.goods.cart.service.impl.CartMapper;
import modoo.module.shop.goods.dlvy.service.OrderDlvyVO;
import modoo.module.shop.goods.dlvy.service.impl.OrderDlvyMapper;
import modoo.module.shop.goods.hist.service.OrderReqHistVO;
import modoo.module.shop.goods.hist.service.impl.OrderReqHistMapper;
import modoo.module.shop.goods.info.service.GoodsCouponService;
import modoo.module.shop.goods.info.service.GoodsCouponVO;
import modoo.module.shop.goods.info.service.GoodsItemVO;
import modoo.module.shop.goods.info.service.GoodsVO;
import modoo.module.shop.goods.info.service.impl.GoodsCouponMapper;
import modoo.module.shop.goods.info.service.impl.GoodsItemMapper;
import modoo.module.shop.goods.info.service.impl.GoodsMapper;
import modoo.module.shop.goods.order.service.OrderGroupVO;
import modoo.module.shop.goods.order.service.OrderItemVO;
import modoo.module.shop.goods.order.service.OrderService;
import modoo.module.shop.goods.order.service.OrderVO;
import modoo.module.shop.goods.setle.service.OrderSetleVO;
import modoo.module.shop.goods.setle.service.impl.OrderSetleMapper;
import modoo.module.shop.user.service.DlvyAdresVO;
import modoo.module.shop.user.service.impl.DlvyAdresMapper;

@Service("orderService")
public class OrderServiceImpl extends EgovAbstractServiceImpl implements OrderService {
	
	
	@Resource(name = "goodsMapper")
	private GoodsMapper goodsMapper;

	@Resource(name = "orderMapper")
	private OrderMapper orderMapper;
	
	@Resource(name = "dlvyAdresMapper")
	private DlvyAdresMapper dlvyAdresMapper;
	
	@Resource(name = "cartMapper")
	private CartMapper cartMapper;
	
	@Resource(name = "orderSetleMapper")
	private OrderSetleMapper orderSetleMapper;
	
	@Resource(name = "orderGroupIdGnrService")
	private EgovIdGnrService orderGroupIdGnrService;
	
	@Resource(name = "orderIdGnrService")
	private EgovIdGnrService orderIdGnrService;
	
	@Resource(name = "orderItemIdGnrService")
	private EgovIdGnrService orderItemIdGnrService;
	
	@Resource(name = "dlvyAdresIdGnrService")
	private EgovIdGnrService dlvyAdresIdGnrService;
	
	@Resource(name="goodsItemMapper")
	private GoodsItemMapper goodsItemMapper;
	
	@Resource(name = "orderDlvyMapper")
	private OrderDlvyMapper orderDlvyMapper;
	
	@Resource(name = "orderReqHistMapper")
	private OrderReqHistMapper orderReqHistMapper;
	
	@Resource(name = "qainfoMapper")
	private QainfoMapper qainfoMapper;
	
	@Resource(name = "qainfoIdGnrService")
	private EgovIdGnrService qainfoIdGnrService;

	@Resource(name = "goodsCouponMapper")
	private GoodsCouponMapper goodsCouponMapper;
	
	/**
	 * ????????????
	 */
	@Override
	public List<OrderVO> selectMyOrderList(OrderVO searchVO) throws Exception {
		List<OrderVO> orderList = orderMapper.selectMyOrderList(searchVO);
		for(OrderVO order: orderList) {
			OrderItemVO orderItem = new OrderItemVO();
			//orderItem.setOrderNo((BigDecimal) order.getOrderNo());
			orderItem.setOrderNo(order.getOrderNo());
			List<OrderItemVO>  orderItemList = (List<OrderItemVO>) orderMapper.selectOrderItemList(order);
			order.setOrderItemList(orderItemList);
			
			//?????????????????? ??????
			List<OrderItemVO> orderQItemList = (List<OrderItemVO>) orderMapper.selectOrderQItemList(order);
			order.setorderQItemList(orderQItemList);
			
			GoodsVO goods = new GoodsVO();
			goods.setGoodsId((String)order.getGoodsId());
			goods=goodsMapper.selectGoods(goods);
			order.setGoods(goods);
		}
		
		return orderList;
	}
	
	/**
	 * ??????????????????
	 */
	@Override
	public HashMap<String, Object> insertOrderGroupPayFinish(OrderGroupVO orderGroup, HashMap<String, Object> infoMap){
		
		
		HashMap<String, Object> payResult = new HashMap<>();
		try {
			//?????? ??????
			java.math.BigDecimal orderGroupNo = orderGroupIdGnrService.getNextBigDecimalId();
			orderGroup.setOrderGroupNo(orderGroupNo);
			orderMapper.insertOrderGroup(orderGroup);
			
			infoMap.put("orderGroupNo", orderGroupNo);
			
			if(orderGroup.isNewDlvyInfo()) { //??????????????????
				DlvyAdresVO dlvyAdres = orderGroup.getDlvyInfo();
				java.math.BigDecimal dadresNo = dlvyAdresIdGnrService.getNextBigDecimalId();
				dlvyAdres.setDadresNo(dadresNo);
				dlvyAdresMapper.insertDlvyAdres(orderGroup.getDlvyInfo());
				
				//?????? ????????? ??????
				if("Y".equals(dlvyAdres.getBassDlvyAt())){
					dlvyAdresMapper.updateRelisBassDlvyAdres(dlvyAdres);
					dlvyAdresMapper.updateSetBassDlvyAdres(dlvyAdres);
				}
			}
			
			//??????
			
			//?????????????????? ??????????????? ?????????????????? 
			ArrayDeque<String> firOrderInfoList = new ArrayDeque<String>();
			//??????????????? ?????????????????? 
			ArrayDeque<String> seOrderInfoList = new ArrayDeque<String>();
			for(OrderVO order : orderGroup.getOrderList()) {
				String orderNo = EgovDateUtil.getToday() + orderIdGnrService.getNextStringId();
				order.setOrderNo(orderNo);
				order.setOrderGroupNo(orderGroupNo);

				//?????? ?????? ??????
				CartItem cartItem = new CartItem();
				cartItem.setCartNo(order.getCartNo());
				List<CartItem> cartItems = (List<CartItem>)cartMapper.selectCartItemList(cartItem);
				java.math.BigDecimal dscntPc = new BigDecimal(0);
				
				if(cartItems.size()!=0){
					for(CartItem ci : cartItems){
						if(ci.getGitemPc()!=null){
							if(ci.getGitemPc().compareTo(java.math.BigDecimal.ZERO) == -1){
								dscntPc = dscntPc.add(ci.getGitemPc());
							}
						}
					}
				}
				//?????? ?????? ?????? ??????
				if("Y".equals(order.getCompnoDscntUseAt())){
					GoodsVO goods = new GoodsVO();
					goods.setGoodsId(order.getGoodsId());
					goods = goodsMapper.selectGoods(goods);
					dscntPc = dscntPc.add(goods.getCompnoDscntPc().multiply(new BigDecimal(order.getOrderCo())));
				}
				order.setDscntAmount(dscntPc);
				orderMapper.insertOrder(order);
				goodsMapper.updateGoodsListSleCo(orderGroupNo);
				
				if(order.getOrderItemList()!=null) {
					StringBuilder firOrderInfo = new StringBuilder();
					StringBuilder seOrderInfo = new StringBuilder();
					
					for(OrderItemVO item : order.getOrderItemList()) {
						java.math.BigDecimal oitemNo = orderItemIdGnrService.getNextBigDecimalId();
						item.setOitemNo(oitemNo);
						item.setOrderNo(order.getOrderNo());
						item.setCartNo(order.getCartNo());
						if(StringUtils.isEmpty(item.getGitemAnswer())) {
							item.setGitemAnswer(null);
						}
						orderMapper.insertOrderItem(item);
	                }
					//????????????????????????
					List<OrderItemVO> orderItemList = (List<OrderItemVO>) orderMapper.selectOrderItemList(order);
					for(OrderItemVO item : orderItemList){
						String sym = "+";
						if(!"Q".equals(item.getGistemSeCode())){
							if(item.getGitemPc().compareTo(java.math.BigDecimal.ZERO)==-1){
								sym = "";
							}
							if("D".equals(item.getGistemSeCode())){
								firOrderInfo.append("????????????:"+item.getGitemNm()+"("+sym+item.getGitemPc()+")");
								seOrderInfo.append("????????????:"+item.getGitemNm()+"("+sym+item.getGitemPc()+")");
							}else if("A".equals(item.getGistemSeCode())){
								firOrderInfo.append("????????????:"+item.getGitemNm()+"("+sym+item.getGitemPc()+")");
								seOrderInfo.append("????????????:"+item.getGitemNm()+"("+sym+item.getGitemPc()+")");
							}else if("F".equals(item.getGistemSeCode())){
								firOrderInfo.append("???????????????:"+item.getGitemNm()+"("+sym+item.getGitemPc()+")");
							}
						}
					}
					firOrderInfoList.add(firOrderInfo.toString());
					seOrderInfoList.add(seOrderInfo.toString());
				}
				//?????? ?????? ????????? ?????? (USE_AT = 'N')
				CartVO cart = new CartVO();
				cart.setCartNo(order.getCartNo());
				cartMapper.updateCartClose(cart);
			}
			infoMap.put("firOrderInfoList",firOrderInfoList);
			infoMap.put("seOrderInfoList",seOrderInfoList);
			 
			OrderVO order = new OrderVO();
			java.math.BigDecimal totOrderPc = new BigDecimal(0);
			
			order.setOrdrrId((String)infoMap.get("userId"));
			order.setOrderGroupNo((BigDecimal)infoMap.get("orderGroupNo"));
			//????????????
			List<OrderVO> resultList = orderMapper.selectMyOrderList(order);
			
			//?????? ?????? ??? ??????
			for(OrderVO od: resultList){
				totOrderPc=totOrderPc.add(od.getTotAmount());
			}

			
			//?????? ??????	
			if(infoMap.get("payMethod").equals("card")){
				payResult.put("success", true);
				
				/*
				if(totOrderPc.compareTo(new BigDecimal(0))==1){
					payResult = cardPay(infoMap, resultList);
					if((Boolean)payResult.get("success")){
						infoMap.put("billKey", (String)payResult.get("billKey"));
						infoMap.put("iniCode", (String)payResult.get("iniCode"));
					}else{
						throw new Exception();
					}
				}else{
					payResult.put("success", true);
				}
				*/
				//this.saveOrderSetleInfo(infoMap, payResult, resultList);
				
				//?????????????????????
				java.math.BigDecimal fOptPc = new BigDecimal(0);
				OrderSetleVO orderSetle = new OrderSetleVO();
				
				ArrayDeque<java.math.BigDecimal> ezPointQue= new ArrayDeque<java.math.BigDecimal>();
				for(OrderVO od : resultList){
					List<OrderItemVO> odItems = (List<OrderItemVO>) orderMapper.selectOrderItemList(od);
					java.math.BigDecimal dscntPc = new BigDecimal(0);
					//?????? ?????? ??????
					if(odItems.size()!=0){
						for(OrderItemVO oi : odItems){
							if(oi.getGitemPc()!=null){
								if(oi.getGitemPc().compareTo(java.math.BigDecimal.ZERO) == -1){
									dscntPc = dscntPc.add(oi.getGitemPc());
								}
							}
							//????????????????????? ??????(-?????? +???)(+?????? -???) ????????????????????? ?????????
							if("F".equals(oi.getGistemSeCode())){
								fOptPc=fOptPc.add(oi.getGitemPc()).multiply(new BigDecimal(-1));
							}
						}
					}
					//1????????? ?????? ?????? ?????? ??????
					if("Y".equals(od.getCompnoDscntUseAt())){
						GoodsVO goods = new GoodsVO();
						goods.setGoodsId(od.getGoodsId());
						goods = goodsMapper.selectGoods(goods);
						dscntPc = dscntPc.add(goods.getCompnoDscntPc().multiply(new BigDecimal(od.getOrderCo())));
					}
					orderSetle.setDscntPc(dscntPc);
					//?????? ????????????
					if((Boolean)payResult.get("success")) {
						orderSetle.setSetleSttusCode("S");	
					}else {
						orderSetle.setSetleSttusCode("F");
					}
					
					//??????????????????
					orderSetle.setFirOrderInfo(firOrderInfoList.poll());
					orderSetle.setSeOrderInfo(seOrderInfoList.poll());
					orderSetle.setSetlePrarnde(null);
					orderSetle.setIsSbs(false);
					orderSetle.setSearchOrderNo(od.getOrderNo());
					orderSetle.setSetleResultCode(String.valueOf(payResult.get("resultCode")));
					orderSetle.setSetleResultMssage(String.valueOf(payResult.get("resultMsg")));
					orderSetle.setSetlePnttm(new Date());
					//????????????
					if("CPN".equals(od.getOrderKndCode())){
						GoodsCouponVO goodsCoupon = new GoodsCouponVO();
						goodsCoupon.setGoodsId(od.getGoodsId());
						goodsCoupon.setOrderCo(od.getOrderCo());
						List<GoodsCouponVO> goodsCouponList = goodsCouponMapper.selectSleGoodsCoupon(goodsCoupon);
						for(GoodsCouponVO gc : goodsCouponList){
							gc.setOrderNo(od.getOrderNo());
							goodsCouponMapper.updateGoodsCouponOrderNo(gc);
						}
					}
					//??????????????????(??????,????????????id)
					//orderMapper.updateOrder(od);
					
					//???????????? ????????? ??????
					//?????? ???????????? ??????	
					if(infoMap.get("payMethod").equals("card")){
						orderSetle.setIniSetleConfmNo((String)infoMap.get("iniCode"));
						orderSetle.setSetleTyCode("CARD");
						orderSetle.setSetleTotAmount(od.getTotAmount().add(dscntPc));
						orderSetle.setSetleCardAmount(od.getTotAmount().add(dscntPc));
						orderSetle.setSetleResultTyCode("CARD");
					}
					
					this.saveOrderSetle(orderSetle);
					//??????????????? ?????? ?????? ?????? ?????? ???????????? ??? ?????? ???????????? ??????
					if("SBS".equals(od.getOrderKndCode())&& StringUtils.isNotEmpty(od.getSbscrptCycleSeCode())){
						od.setNextSetlede(nextSbsDate(od));
						od.setNowOdr(od.getNowOdr()+1);
						//????????????,?????? ??????
						orderMapper.updateOrder(od);
						//????????? ?????? ??????
						orderSetle.setSetleTotAmount(od.getTotAmount().add(dscntPc).add(fOptPc));
						orderSetle.setSetlePrarnde(nextSbsDate(od));
						orderSetle.setIsSbs(true);
						this.saveOrderSetle(orderSetle);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
		return payResult;
	}


	//?????????????????? ??????
	@Override
	public BigDecimal orderGroupNo(OrderGroupVO orderGroup) {
		try {
			if (orderGroup.getOrderGroupNo() == null) {
				return orderGroupIdGnrService.getNextBigDecimalId();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return orderGroup.getOrderGroupNo();
	}
	
	/**
	 * ??????????????????
	 */
	@Override
	@Transactional
	public HashMap<String, Object> insertOrderGroup(OrderGroupVO orderGroup, HashMap<String, Object> infoMap){
		
		//?????? ????????????
		HashMap<String, Object> payResult = new HashMap<>();
		try {
			//?????? ??????
			java.math.BigDecimal orderGroupNo = orderGroupIdGnrService.getNextBigDecimalId();
			orderGroup.setOrderGroupNo(orderGroupNo);
			orderMapper.insertOrderGroup(orderGroup);
			
			infoMap.put("orderGroupNo", orderGroupNo);
			
			if(orderGroup.isNewDlvyInfo()) { //??????????????????
				DlvyAdresVO dlvyAdres = orderGroup.getDlvyInfo();
				java.math.BigDecimal dadresNo = dlvyAdresIdGnrService.getNextBigDecimalId();
				dlvyAdres.setDadresNo(dadresNo);
				dlvyAdresMapper.insertDlvyAdres(orderGroup.getDlvyInfo());
				
				//?????? ????????? ??????
				if("Y".equals(dlvyAdres.getBassDlvyAt())){
					dlvyAdresMapper.updateRelisBassDlvyAdres(dlvyAdres);
					dlvyAdresMapper.updateSetBassDlvyAdres(dlvyAdres);
				}
			}
			
			//??????
			
			//?????????????????? ??????????????? ?????????????????? 
			ArrayDeque<String> firOrderInfoList = new ArrayDeque<String>();
			//??????????????? ?????????????????? 
			ArrayDeque<String> seOrderInfoList = new ArrayDeque<String>();
			for(OrderVO order : orderGroup.getOrderList()) {
				//java.math.BigDecimal orderNo = orderIdGnrService.getNextBigDecimalId(); 
				String orderNo = EgovDateUtil.getToday() + orderIdGnrService.getNextStringId();
				order.setOrderNo(orderNo);
				order.setOrderGroupNo(orderGroupNo);

				//?????? ?????? ??????
				CartItem cartItem = new CartItem();
				cartItem.setCartNo(order.getCartNo());
				List<CartItem> cartItems = (List<CartItem>)cartMapper.selectCartItemList(cartItem);
				java.math.BigDecimal dscntPc = new BigDecimal(0);
				
				if(cartItems.size()!=0){
					for(CartItem ci : cartItems){
						if(ci.getGitemPc()!=null){
							if(ci.getGitemPc().compareTo(java.math.BigDecimal.ZERO) == -1){
								dscntPc = dscntPc.add(ci.getGitemPc());
							}
						}
					}
				}
				//?????? ?????? ?????? ??????
				if("Y".equals(order.getCompnoDscntUseAt())){
					GoodsVO goods = new GoodsVO();
					goods.setGoodsId(order.getGoodsId());
					goods = goodsMapper.selectGoods(goods);
					dscntPc = dscntPc.add(goods.getCompnoDscntPc().multiply(new BigDecimal(order.getOrderCo())));
				}
				order.setDscntAmount(dscntPc);
				orderMapper.insertOrder(order);
				goodsMapper.updateGoodsListSleCo(orderGroupNo);
				
				if(order.getOrderItemList()!=null) {
					StringBuilder firOrderInfo = new StringBuilder();
					StringBuilder seOrderInfo = new StringBuilder();
					
					for(OrderItemVO item : order.getOrderItemList()) {
						java.math.BigDecimal oitemNo = orderItemIdGnrService.getNextBigDecimalId();
						item.setOitemNo(oitemNo);
						item.setOrderNo(order.getOrderNo());
						item.setCartNo(order.getCartNo());
						if(StringUtils.isEmpty(item.getGitemAnswer())) {
							item.setGitemAnswer(null);
						}
						orderMapper.insertOrderItem(item);
	                }
					//????????????????????????
					List<OrderItemVO> orderItemList = (List<OrderItemVO>) orderMapper.selectOrderItemList(order);
					for(OrderItemVO item : orderItemList){
						String sym = "+";
						if(!"Q".equals(item.getGistemSeCode())){
							if(item.getGitemPc().compareTo(java.math.BigDecimal.ZERO)==-1){
								sym = "";
							}
							if("D".equals(item.getGistemSeCode())){
								firOrderInfo.append("????????????:"+item.getGitemNm()+"("+sym+item.getGitemPc()+")");
								seOrderInfo.append("????????????:"+item.getGitemNm()+"("+sym+item.getGitemPc()+")");
							}else if("A".equals(item.getGistemSeCode())){
								firOrderInfo.append("????????????:"+item.getGitemNm()+"("+sym+item.getGitemPc()+")");
								seOrderInfo.append("????????????:"+item.getGitemNm()+"("+sym+item.getGitemPc()+")");
							}else if("F".equals(item.getGistemSeCode())){
								firOrderInfo.append("???????????????:"+item.getGitemNm()+"("+sym+item.getGitemPc()+")");
							}
						}
					}
					firOrderInfoList.add(firOrderInfo.toString());
					seOrderInfoList.add(seOrderInfo.toString());
				}
				//?????? ?????? ????????? ?????? (USE_AT = 'N')
				CartVO cart = new CartVO();
				cart.setCartNo(order.getCartNo());
				cartMapper.updateCartClose(cart);
			}
			infoMap.put("firOrderInfoList",firOrderInfoList);
			infoMap.put("seOrderInfoList",seOrderInfoList);
			 
			OrderVO order = new OrderVO();
			java.math.BigDecimal totOrderPc = new BigDecimal(0);
			
			order.setOrdrrId((String)infoMap.get("userId"));
			order.setOrderGroupNo((BigDecimal)infoMap.get("orderGroupNo"));
			//????????????
			List<OrderVO> resultList = orderMapper.selectMyOrderList(order);
			
			//?????? ?????? ??? ??????
			for(OrderVO od: resultList){
				totOrderPc=totOrderPc.add(od.getTotAmount());
			}

			//??????????????? ??? ????????? ?????? ?????? ?????????
			ArrayDeque<String> ezCodeList = new ArrayDeque<String>();
			//????????? ?????? ?????? ????????? 
			ArrayDeque<java.math.BigDecimal> ezPointQue= new ArrayDeque<java.math.BigDecimal>();
			//????????? ?????? ??????
			String ezCode = "";
			Boolean isEzCodes = false;
			//?????????????????? ??????
			if(infoMap.get("payMethod").equals("point")){
				payResult = ezwelPointPay(infoMap, resultList);
				if((Boolean)payResult.get("success")){
					//????????? ?????? ????????????(??????????????????)
					if((Boolean)payResult.get("ezCodes")){
						ezCodeList=(ArrayDeque<String>) payResult.get("ezCodeList");
						isEzCodes=true;
					}else{
						ezCode = (String)payResult.get("ezCode"); 
					}
					//????????? ????????? ?????? ???????????? ?????????????????????.
					Biling biling = new Biling();
					payResult=biling.bilingKey(infoMap);
					infoMap.put("billKey", payResult.get("billKey"));
					payResult.put("ezCode", ezCode);
					payResult.put("ezCodes", isEzCodes);
					payResult.put("ezCodeList",ezCodeList);
					//?????? ?????? ????????? ?????? ?????? ?????? ??????
					if((Boolean)payResult.get("success")){
						this.saveOrderSetleInfo(infoMap, payResult, resultList);
					}else{
						throw new Exception();
					}
				}
			//?????? ??????	
			}else if(infoMap.get("payMethod").equals("card")){
				//????????? ????????? ???
				if((Boolean)infoMap.get("isEzwel")){
					infoMap.put("isCard", true);
					//???????????? 0????????? ???????????? X 
					if(totOrderPc.compareTo(new BigDecimal(0))==0){
						payResult.put("success", true);
					}else{
						payResult = cardPay(infoMap, resultList);
					}
					if((Boolean)payResult.get("success")){
						if(payResult.get("billKey")!=null && payResult.get("iniCode")!=null){
							infoMap.put("billKey", (String)payResult.get("billKey"));
							infoMap.put("iniCode", (String)payResult.get("iniCode"));
						}
						payResult = ezwelPointPay(infoMap, resultList);
						//?????? ??????????????? ????????? ????????????
						if((Boolean)payResult.get("ezCodes")){
							ezCodeList=(ArrayDeque<String>) payResult.get("ezCodeList");
							isEzCodes=true;
						}else{
							ezCode = (String)payResult.get("ezCode"); 
						}
						payResult.put("ezCode", ezCode);
						payResult.put("ezCodes", isEzCodes);
						payResult.put("ezCodeList",ezCodeList);
						this.saveOrderSetleInfo(infoMap, payResult, resultList);
					}else{
						throw new Exception();
					}
				}else{
					if(totOrderPc.compareTo(new BigDecimal(0))==1){
						payResult = cardPay(infoMap, resultList);
						if((Boolean)payResult.get("success")){
							infoMap.put("billKey", (String)payResult.get("billKey"));
							infoMap.put("iniCode", (String)payResult.get("iniCode"));
						}else{
							throw new Exception();
						}
					}else{
						payResult.put("success", true);
					}
					this.saveOrderSetleInfo(infoMap, payResult, resultList);
				}
			//?????????,?????? ??????
			}else if(infoMap.get("payMethod").equals("both")){
				ArrayDeque<java.math.BigDecimal> ezPointStored= new ArrayDeque<java.math.BigDecimal>();
				EzwelFunc ezFunc = new EzwelFunc();
				java.math.BigDecimal ezwelPoint = ezFunc.ezwelPointSearch((String)infoMap.get("userKey"), (String)infoMap.get("clientCd"));
				for(OrderVO od : resultList) {
					//????????? ???????????????
					if(ezwelPoint.compareTo(java.math.BigDecimal.ZERO)==1){
						//???????????? ??????????????? ??????
						if(ezwelPoint.compareTo(od.getTotAmount().add(od.getDscntAmount()))==1){
							ezPointQue.add(od.getTotAmount().add(od.getDscntAmount()));
							ezwelPoint=ezwelPoint.subtract(od.getTotAmount().add(od.getDscntAmount()));
						//???????????? ??????????????? ??????	
						}else{
							ezPointQue.add(ezwelPoint);
							ezwelPoint=java.math.BigDecimal.ZERO;
						}
					}else{
						ezPointQue.add(java.math.BigDecimal.ZERO);
					}
				}
				infoMap.put("ezPointQue", ezPointQue);
				payResult = cardPay(infoMap, resultList);
				
				if((Boolean)payResult.get("success")){
					infoMap.put("billKey", payResult.get("billKey"));
					infoMap.put("iniCode", payResult.get("iniCode"));
					payResult = ezwelPointPay(infoMap, resultList);
					if((Boolean)payResult.get("ezCodes")){
						ezCodeList=(ArrayDeque<String>) payResult.get("ezCodeList");
						isEzCodes=true;
						ezPointQue= (ArrayDeque<java.math.BigDecimal>) payResult.get("ezPointQue");
						ezPointStored = (ArrayDeque<java.math.BigDecimal>) payResult.get("ezPointStored");
					}else{
						ezCode = (String)payResult.get("ezCode"); 
					}
					payResult.put("ezCode", ezCode);
					payResult.put("ezCodeList",ezCodeList);
					payResult.put("ezPointQue",ezPointStored);
					payResult.put("ezCodes", isEzCodes);
					this.saveOrderSetleInfo(infoMap, payResult, resultList);
				}else{
					throw new Exception();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
		}
		return payResult;
	}

	/**
	 * ??????????????????(???????????? ???????????? --> ?????????????????????)
	 *
	 * @return
	 */
	@Override
	@Transactional
	public HashMap<String, Object> insertPaymentOrderGroup(HttpServletRequest request, OrderGroupVO orderGroup) {

		HashMap<String, Object> resultMap = new HashMap<>();

		LoginVO user = (LoginVO) EgovUserDetailsHelper.getAuthenticatedUser();

		try {
			//?????? ??????
			java.math.BigDecimal orderGroupNo = orderGroupNo(orderGroup);
			orderGroup.setOrderGroupNo(orderGroupNo);
			orderMapper.insertOrderGroup(orderGroup);

			resultMap.put("orderGroupNo", orderGroupNo);

			if (orderGroup.isNewDlvyInfo()) { //??????????????????
				DlvyAdresVO dlvyAdres = orderGroup.getDlvyInfo();
				java.math.BigDecimal dadresNo = dlvyAdresIdGnrService.getNextBigDecimalId();
				dlvyAdres.setDadresNo(dadresNo);
				dlvyAdresMapper.insertDlvyAdres(orderGroup.getDlvyInfo());

				//?????? ????????? ??????
				if ("Y".equals(dlvyAdres.getBassDlvyAt())) {
					dlvyAdresMapper.updateRelisBassDlvyAdres(dlvyAdres);
					dlvyAdresMapper.updateSetBassDlvyAdres(dlvyAdres);
				}
			}

			//??????

			//?????????????????? ??????????????? ??????????????????
			ArrayDeque<String> firOrderInfoList = new ArrayDeque<String>();
			//??????????????? ??????????????????
			ArrayDeque<String> seOrderInfoList = new ArrayDeque<String>();
			for (OrderVO order : orderGroup.getOrderList()) {
				//java.math.BigDecimal orderNo = orderIdGnrService.getNextBigDecimalId();
				String orderNo = EgovDateUtil.getToday() + orderIdGnrService.getNextStringId();
				order.setOrderNo(orderNo);
				order.setOrderGroupNo(orderGroupNo);

				//?????? ?????? ??????x
				CartItem cartItem = new CartItem();
				cartItem.setCartNo(order.getCartNo());
				List<CartItem> cartItems = (List<CartItem>) cartMapper.selectCartItemList(cartItem);
				java.math.BigDecimal dscntPc = new BigDecimal(0);

				if (cartItems.size() != 0) {
					for (CartItem ci : cartItems) {
						if (ci.getGitemPc() != null) {
							if (ci.getGitemPc().compareTo(java.math.BigDecimal.ZERO) == -1) {
								dscntPc = dscntPc.add(ci.getGitemPc());
							}
						}
					}
				}
				//?????? ?????? ?????? ??????
				if ("Y".equals(order.getCompnoDscntUseAt())) {
					GoodsVO goods = new GoodsVO();
					goods.setGoodsId(order.getGoodsId());
					goods = goodsMapper.selectGoods(goods);
					dscntPc = dscntPc.add(goods.getCompnoDscntPc().multiply(new BigDecimal(order.getOrderCo())));
				}
				order.setDscntAmount(dscntPc);
				order.setOrderSttusCode("ST100");

				System.out.println("==================== insertOrder =====================");
				System.out.println(order.toString());
				orderMapper.insertOrder(order);
				//goodsMapper.updateGoodsListSleCo(orderGroupNo);

				if (order.getOrderItemList() != null) {
					StringBuilder firOrderInfo = new StringBuilder();
					StringBuilder seOrderInfo = new StringBuilder();

					for (OrderItemVO item : order.getOrderItemList()) {
						java.math.BigDecimal oitemNo = orderItemIdGnrService.getNextBigDecimalId();
						item.setOitemNo(oitemNo);
						item.setOrderNo(order.getOrderNo());
						item.setCartNo(order.getCartNo());
						if (StringUtils.isEmpty(item.getGitemAnswer())) {
							item.setGitemAnswer(null);
						}
						orderMapper.insertOrderItem(item);
					}
				}
			}
			OrderVO order = new OrderVO();
			java.math.BigDecimal totOrderPc = new BigDecimal(0);

			order.setOrdrrId(user.getId());
			order.setOrderGroupNo(orderGroupNo);
			//????????????
			List<OrderVO> resultList = orderMapper.selectMyOrderList(order);

			//?????? ?????? ??? ??????
			for (OrderVO od : resultList) {
				totOrderPc = totOrderPc.add(od.getTotAmount());
			}

			/*if ("both".equals(orderGroup.getPayMethod())) {
				java.math.BigDecimal ezPoint = null;
				if (user.getGroupId().equals(PartnerGroupId.EZWEL.getCode()))
					ezPoint = new EzwelFunc().ezwelPointSearch(user.getUserKey(), user.getClientCd());
				else if (user.getGroupId().equals(PartnerGroupId.EXANADU.getCode())) {
					ezPoint = ExanaduSSO.pointSearch(request);
					//ezPoint = new BigDecimal(50); // TODO ???????????? ????????????
				} else {
					KcpPayment kcpPayment = new KcpPayment();
					kcpInfoVO = kcpPayment.loadKcpPoint(kcpInfoVO);
					ezPoint = new BigDecimal(kcpInfoVO.getKcpResult().getRsv_pnt());
					//ezPoint=new BigDecimal("900");
				}
				System.out.println("?????? ????????? ?????? --- " + ezPoint);
				if (ezPoint.compareTo(BigDecimal.ZERO) == 1) {
					totOrderPc = totOrderPc.subtract(ezPoint);
				}

			}*/
			System.out.println("?????? ???????????? ???????????? --- " + totOrderPc.toPlainString());

			//???????????? ?????? ????????????
			if (resultList != null) {
				resultMap.put("price", totOrderPc);
				resultMap.put("oid", orderGroupNo);
				resultMap.put("buyertel", resultList.get(0).getTelno());
				resultMap.put("buyeremail", resultList.get(0).getOrdrrEmail());
				if (resultList.size() == 1) {
					GoodsVO goods = new GoodsVO();
					goods.setGoodsId(resultList.get(0).getGoodsId());
					goods = goodsMapper.selectGoods(goods);
					resultMap.put("goodname", goods.getGoodsNm());
				} else if (resultList.size() > 1) {
					resultMap.put("goodname", "???????????????" + resultList.size() + "?????? ??????");
				}
			}
		} catch (Exception e) {
			System.out.println("=======================?????? ??????==========================");
			e.printStackTrace();
		}
		return resultMap;
	}

	/**
	 * ????????????
	 */
	@Override
	public JsonResult paymentInfo(JsonResult jsonResult, HashMap<String, Object> resultMap, HttpServletRequest req) throws Exception {
		String mid = EgovProperties.getProperty("INICIS.mid");        // ????????? ID(????????? ????????? ??????)
		//??????
		String signKey = EgovProperties.getProperty("INICIS.signKey");
		;    // ?????? key
		String timestamp = SignatureUtil.getTimestamp();            // util??? ????????? ????????????
		String oid = String.valueOf(resultMap.get("oid"));    // ????????? ????????????(??????????????? ?????? ??????)
		String price = String.valueOf(resultMap.get("price"));                                                    // ????????????(???????????? ??????, ??????????????? ?????? ??????)
		String goodname = String.valueOf(resultMap.get("goodname"));                                                    // ????????????(???????????? ??????, ??????????????? ?????? ??????)
		String gopaymethod = String.valueOf(resultMap.get("gopayMethod"));
		String payMethod = String.valueOf(resultMap.get("payMethod"));
		String isCart = String.valueOf(resultMap.get("isCart"));
//		String gopaymethod			= "Card:DirectBank:HPP:Culture:DGCL:Bcsh:HPMN";													// ????????????(???????????? ??????, ??????????????? ?????? ??????)
		String acceptmethod = "";                                                    // ????????????(???????????? ??????, ??????????????? ?????? ??????)
		if ("Card".equals(resultMap.get("gopayMethod"))) acceptmethod = "below1000";
		if ("HPP".equals(resultMap.get("gopayMethod"))) acceptmethod = "HPP(2)";

		String cardNoInterestQuota = "11-2:3:,34-5:12,14-6:12:24,12-12:36,06-9:12,01-3:4";        // ?????? ????????? ?????? ??????(??????????????? ?????? ??????)
		String cardQuotaBase = "2:3:4:5:6:11:12:24:36";        // ??????????????? ????????? ?????? ????????? ??????

		//###############################################
		// 2. ????????? ????????? ?????? signKey??? ??????????????? ?????? (SHA-256?????? ??????)
		//###############################################
		String mKey = SignatureUtil.hash(signKey, "SHA-256");

		//###############################################
		// 2.signature ??????
		//###############################################
		Map<String, String> signParam = new HashMap<String, String>();

		signParam.put("oid", oid);                    // ??????
		signParam.put("price", price);                // ??????
		signParam.put("timestamp", timestamp);        // ??????

		// signature ????????? ?????? (???????????? ???????????? signParam??? ????????? ????????? ????????? NVP ???????????? ????????? hash)
		String signature = SignatureUtil.makeSignature(signParam);
		/* ?????? */
		String siteDomain = req.getScheme() + "://" + req.getServerName(); //????????? ????????? ??????; //????????? ????????? ??????

		/*StringBuffer url = req.getRequestURL();
		String uri = req.getRequestURI();
		int idx = (((uri != null) && (uri.length() > 0)) ? url.indexOf(uri) : url.length());
		String host = ""; //base url
		String domain = url.substring(0, idx); //base url
		idx = host.indexOf("://");
		System.out.println(uri);
		if(idx > 0) {
		  domain = host.substring(0,idx); //remove scheme if present
		}*/
		if (req.getServerName().contains("modoo.ai")) {
			siteDomain = "https://" + req.getServerName(); //????????? ????????? ??????
		}

		/*???????????? ???????????????*/
		/*StringBuilder cartnoStr = (StringBuilder)resultMap.get("cartnoStr");*/

		StringBuilder cartnoStr = (StringBuilder) resultMap.get("cartnoStr");
		/*for(int i=0;i<cartnoArr.size();i++){
			if(i==0){
				cartnoStr.append("?cartno="+cartnoArr.get(i));
			}
			cartnoStr.append("&cartno="+cartnoArr.get(i));
		}*/

		String closeUrl = siteDomain + "/shop/goods/closeInisis.do?orderGroupNo=" + oid;
		String popupUrl = siteDomain + "/inisis/popup.jsp";
		String returnUrl = siteDomain + "/shop/goods/inisisResponse.do" + cartnoStr + "&orderGroupNo=" + oid + "&payMethod=" + payMethod + "&gopayMethod=" + gopaymethod + "&isCart=" + isCart;

		jsonResult.put("acceptmethod", acceptmethod);
		jsonResult.put("gopaymethod", gopaymethod);
		jsonResult.put("returnUrl", siteDomain);
		jsonResult.put("closeUrl", closeUrl);
		jsonResult.put("popupUrl", popupUrl);
		jsonResult.put("signature", signature);
		jsonResult.put("buyertel", String.valueOf(resultMap.get("buyertel")));
		jsonResult.put("mKey", mKey);
		jsonResult.put("cardQuotaBase", cardQuotaBase);
		jsonResult.put("cardNoInterestQuota", cardNoInterestQuota);
		jsonResult.put("goodname", goodname);
		jsonResult.put("price", price);
		jsonResult.put("oid", oid);
		jsonResult.put("timestamp", timestamp);
		jsonResult.put("signKey", signKey);
		jsonResult.put("mid", mid);
		jsonResult.put("returnUrl", returnUrl);
		// ????????? URL?????? ????????? ????????? ?????????.
		// Ex) returnURL??? http://localhost:8080INIpayStdSample/INIStdPayReturn.jsp ??????
		// http://localhost:8080/INIpayStdSample ????????? ????????????.

		//?????? ??????(card,point,both)
		return jsonResult;
	}

	/**
	 * ?????? ?????? ??????
	 *
	 * @param infoMap
	 * @param payResult
	 * @param resultList
	 * @throws Exception
	 */
	@Override
	public void saveOrderSetleInfo(HashMap<String, Object> infoMap, HashMap<String, Object> payResult,
								   List<OrderVO> resultList) throws Exception {

		System.out.println("=================== saveOrderSetleInfo ===================");

		//?????????????????????
		java.math.BigDecimal fOptPc = new BigDecimal(0);
		OrderSetleVO orderSetle = new OrderSetleVO();
		//???????????????????????????
		ArrayDeque<String> firOrderInfoList = new ArrayDeque<String>();
		//???????????????????????????
		ArrayDeque<String> seOrderInfoList = new ArrayDeque<String>();

		//????????? ??????
		ArrayDeque<String> ezCodeList = new ArrayDeque<String>();
		ArrayDeque<java.math.BigDecimal> ezPointQue = new ArrayDeque<java.math.BigDecimal>();
		for (OrderVO od : resultList) {
			/*List<OrderItemVO> odItems = (List<OrderItemVO>) orderMapper.selectOrderItemList(od);*/
			java.math.BigDecimal dscntPc = new BigDecimal(0);

			StringBuilder firOrderInfo = new StringBuilder();
			StringBuilder seOrderInfo = new StringBuilder();

			//????????????????????????
			List<OrderItemVO> orderItemList = (List<OrderItemVO>) orderMapper.selectOrderItemList(od);
			for (OrderItemVO item : orderItemList) {
				String sym = "+";
				if (!"Q".equals(item.getGistemSeCode())) {
					if (item.getGitemPc().compareTo(java.math.BigDecimal.ZERO) == -1) {
						sym = "";
					}
					if ("D".equals(item.getGistemSeCode())) {
						firOrderInfo.append("????????????:" + item.getGitemNm() + "(" + sym + item.getGitemPc() + ")");
						seOrderInfo.append("????????????:" + item.getGitemNm() + "(" + sym + item.getGitemPc() + ")");
					} else if ("A".equals(item.getGistemSeCode())) {
						firOrderInfo.append("????????????:" + item.getGitemNm() + "(" + sym + item.getGitemPc() + ")");
						seOrderInfo.append("????????????:" + item.getGitemNm() + "(" + sym + item.getGitemPc() + ")");
					} else if ("F".equals(item.getGistemSeCode())) {
						firOrderInfo.append("???????????????:" + item.getGitemNm() + "(" + sym + item.getGitemPc() + ")");
					}
				}
			}
			firOrderInfoList.add(firOrderInfo.toString());
			seOrderInfoList.add(seOrderInfo.toString());

			//?????? ?????? ??????
			if (orderItemList.size() != 0) {
				for (OrderItemVO oi : orderItemList) {
					if (oi.getGitemPc() != null) {
						if (oi.getGitemPc().compareTo(java.math.BigDecimal.ZERO) == -1) {
							dscntPc = dscntPc.add(oi.getGitemPc());
						}
					}
					//????????????????????? ??????(-?????? +???)(+?????? -???) ????????????????????? ?????????
					if ("F".equals(oi.getGistemSeCode())) {
						fOptPc = fOptPc.add(oi.getGitemPc()).multiply(new BigDecimal(-1));
					}
				}
			}
			//1????????? ?????? ?????? ?????? ??????
		/*	if ("Y".equals(od.getCompnoDscntUseAt())) {
				GoodsVO goods = new GoodsVO();
				goods.setGoodsId(od.getGoodsId());
				goods = goodsMapper.selectGoods(goods);
				java.math.BigDecimal compnoDscntPc = new BigDecimal(0);
				if ("Y".equals(od.getExprnUseAt())) {
					compnoDscntPc = goods.getExprnCompnoDscntPc();
				} else {
					compnoDscntPc = goods.getCompnoDscntPc();
				}
				dscntPc = dscntPc.add(compnoDscntPc.multiply(new BigDecimal(od.getOrderCo())));
			}*/
			orderSetle.setDscntPc(dscntPc);
			//?????? ????????????
			if ((Boolean) payResult.get("success")) {
				orderSetle.setSetleSttusCode("S");
				od.setOrderSttusCode("ST02");
				if (infoMap.get("gopayMethod") != null) {   //????????????????????????
					orderSetle.setGoPayMethod(String.valueOf(infoMap.get("gopayMethod")));
				}
			} else {
				od.setOrderSttusCode("ST100");
				orderSetle.setSetleSttusCode("F");
			}
			od.setBillKey((String) infoMap.get("billKey"));
			od.setCardId((String) infoMap.get("cardId"));
			//??????????????????
			orderSetle.setFirOrderInfo(firOrderInfoList.poll());
			orderSetle.setSeOrderInfo(seOrderInfoList.poll());
			orderSetle.setSetlePrarnde(null);
			orderSetle.setIsSbs(false);
			orderSetle.setSearchOrderNo(od.getOrderNo());
			orderSetle.setSetleResultCode(String.valueOf(payResult.get("resultCode")));
			orderSetle.setSetleResultMssage(String.valueOf(payResult.get("resultMsg")));
			orderSetle.setSetlePnttm(new Date());
			//????????????
			if ("CPN".equals(od.getOrderKndCode())) {
				GoodsCouponVO goodsCoupon = new GoodsCouponVO();
				goodsCoupon.setGoodsId(od.getGoodsId());
				goodsCoupon.setOrderCo(od.getOrderCo());
				List<GoodsCouponVO> goodsCouponList = goodsCouponMapper.selectSleGoodsCoupon(goodsCoupon);
				for (GoodsCouponVO gc : goodsCouponList) {
					gc.setOrderNo(od.getOrderNo());
					goodsCouponMapper.updateGoodsCouponOrderNo(gc);
				}
			}
			//??????????????????(??????,????????????id)
			if (StringUtils.isEmpty(od.getCardId())) {
				od.setCardId(null);
			}
			orderMapper.updateOrder(od);


			/**
			 * EZP : ??????????????? ??????
			 * EZCD, BNCD, EXCD : ?????????+?????? ??????
			 * CARD : ???????????? ??????
			 **/
			//???????????? ????????? ??????
			//?????? ????????? ??? ?????? ??????
		/*	if ("both".equals(infoMap.get("payMethod"))) {
				orderSetle.setIniSetleConfmNo((String) infoMap.get("iniCode"));
				//????????????

				if (payResult.get("ezCodes") != null && payResult.get("ezCode") != null) {
					if ((Boolean) payResult.get("ezCodes")) {
						ezCodeList = (ArrayDeque<String>) payResult.get("ezCodeList");
						orderSetle.setEzwSetleConfmNo(ezCodeList.poll());

						ezPointQue = (ArrayDeque<java.math.BigDecimal>) payResult.get("ezPointQue");

						System.out.println("ezPointQue:" + ezPointQue);

						orderSetle.setSetlePoint(ezPointQue.peek());

						//???????????? 0??? ?????????
						if (ezPointQue.peek().compareTo(java.math.BigDecimal.ZERO) != 0) {
							orderSetle.setSetleTyCode("EZCD");
							orderSetle.setSetleResultTyCode("EZCD");
							if (PartnerGroupId.BENEPIA.getCode().equals(infoMap.get("partnerId"))) {
								orderSetle.setSetleTyCode("BNCD");
								orderSetle.setSetleResultTyCode("BNCD");
							} else if (PartnerGroupId.EXANADU.getCode().equals(infoMap.get("partnerId"))) {
								orderSetle.setSetleTyCode("EXCD");
								orderSetle.setSetleResultTyCode("EXCD");
								orderSetle.setCustId((String) payResult.get("custId"));
							}
						} else {
							orderSetle.setSetleTyCode("CARD");
							orderSetle.setSetleResultTyCode("CARD");
							if("HPP".equals(infoMap.get("gopayMethod")) || "MOBILE".equals(infoMap.get("gopayMethod"))){
								orderSetle.setSetleTyCode("HPP");
								orderSetle.setSetleResultTyCode("HPP");
							}
						}
						orderSetle.setSetleCardAmount(od.getTotAmount().subtract(ezPointQue.poll()));

					} else {

						if (BigDecimal.ZERO.compareTo((BigDecimal) infoMap.get("useEzwelPoint")) != 0) {
							orderSetle.setSetleTyCode("EZCD");
							orderSetle.setSetleResultTyCode("EZCD");
							if (PartnerGroupId.BENEPIA.getCode().equals(infoMap.get("partnerId"))) {
								orderSetle.setSetleTyCode("BNCD");
								orderSetle.setSetleResultTyCode("BNCD");
							} else if (PartnerGroupId.EXANADU.getCode().equals(infoMap.get("partnerId"))) {
								orderSetle.setSetleTyCode("EXCD");
								orderSetle.setSetleResultTyCode("EXCD");
								orderSetle.setCustId((String) payResult.get("custId"));
							}
						} else {
							orderSetle.setSetleTyCode("CARD");
							orderSetle.setSetleResultTyCode("CARD");
							if("HPP".equals(infoMap.get("gopayMethod")) || "MOBILE".equals(infoMap.get("gopayMethod"))){
								orderSetle.setSetleTyCode("HPP");
								orderSetle.setSetleResultTyCode("HPP");
							}
						}
						orderSetle.setEzwSetleConfmNo((String) payResult.get("ezCode"));
						orderSetle.setSetlePoint((BigDecimal) infoMap.get("useEzwelPoint"));
						orderSetle.setSetleCardAmount(od.getTotAmount().add(dscntPc).subtract((BigDecimal) infoMap.get("useEzwelPoint")));
					}
				}
				orderSetle.setSetleTotAmount(od.getTotAmount().add(dscntPc));

				//????????? ???????????? ??????
			} else if ("point".equals(infoMap.get("payMethod"))) {
				if (payResult.get("ezCodes") != null && payResult.get("ezCode") != null) {
					if ((Boolean) payResult.get("ezCodes")) {
						ezCodeList = (ArrayDeque<String>) payResult.get("ezCodeList");
						orderSetle.setEzwSetleConfmNo(ezCodeList.poll());
					} else {
						orderSetle.setEzwSetleConfmNo((String) payResult.get("ezCode"));
					}
				}
				orderSetle.setSetleTyCode("EZP");
				orderSetle.setSetleTotAmount(od.getTotAmount().add(dscntPc));
				orderSetle.setSetlePoint(od.getTotAmount().add(dscntPc));
				orderSetle.setSetleCardAmount(java.math.BigDecimal.ZERO);
				orderSetle.setSetleResultTyCode("EZP");
				if (PartnerGroupId.BENEPIA.getCode().equals(infoMap.get("partnerId"))) {
					orderSetle.setSetleTyCode("BNP");
					orderSetle.setSetleResultTyCode("BNP");
				} else if (PartnerGroupId.EXANADU.getCode().equals(infoMap.get("partnerId"))) {
					orderSetle.setSetleTyCode("EXA");
					orderSetle.setSetleResultTyCode("EXA");
					orderSetle.setCustId((String) payResult.get("custId"));
				}

				//?????? ???????????? ??????
			}*/if ("card".equals(infoMap.get("payMethod"))) {
				if (payResult.get("ezCodes") != null && payResult.get("ezCode") != null) {
					if ((Boolean) infoMap.get("isEzwel")) {
						if ((Boolean) payResult.get("ezCodes")) {
							ezCodeList = (ArrayDeque<String>) payResult.get("ezCodeList");
							orderSetle.setEzwSetleConfmNo(ezCodeList.poll());
						}
						/*else{
							orderSetle.setEzwSetleConfmNo((String)payResult.get("ezCode"));
						}*/
					} else if ((Boolean) infoMap.get("isExanadu")) {
						if ((Boolean) payResult.get("ezCodes")) {
							ezCodeList = (ArrayDeque<String>) payResult.get("ezCodeList");
							orderSetle.setEzwSetleConfmNo(ezCodeList.poll());
						}
					}
					orderSetle.setSetlePoint(java.math.BigDecimal.ZERO);
				}
				orderSetle.setIniSetleConfmNo((String) infoMap.get("iniCode"));
				orderSetle.setSetleTyCode("CARD");
				orderSetle.setSetleResultTyCode("CARD");
				if("HPP".equals(infoMap.get("gopayMethod")) || "MOBILE".equals(infoMap.get("gopayMethod"))){
					orderSetle.setSetleTyCode("HPP");
					orderSetle.setSetleResultTyCode("HPP");
				}
				orderSetle.setSetleTotAmount(od.getTotAmount());
				orderSetle.setSetleCardAmount(od.getTotAmount());

			}
			this.saveOrderSetle(orderSetle);
			//??????????????? ?????? ?????? ?????? ?????? ???????????? ??? ?????? ???????????? ??????
			if ("SBS".equals(od.getOrderKndCode()) && StringUtils.isNotEmpty(od.getSbscrptCycleSeCode()) && (Boolean) payResult.get("success")) {
				od.setNextSetlede(nextSbsDate(od));
				od.setNowOdr(od.getNowOdr() + 1);
				//????????????,?????? ??????
				orderMapper.updateOrder(od);
				//????????? ?????? ??????
				orderSetle.setSetleTotAmount(od.getTotAmount().add(fOptPc));
				orderSetle.setSetlePrarnde(nextSbsDate(od));
				orderSetle.setIsSbs(true);
			/* else if(PartnerGroupId.EXANADU.getCode().equals(infoMap.get("partnerId"))) {
					orderSetle.setSetleTyCode("CARD");
					orderSetle.setSetleResultTyCode("CARD");
				}*/
				this.saveOrderSetle(orderSetle);
			}
		}
	}
	
	
	
	/**
	 * ???????????? ??????
	 * @param orderSetle
	 * @throws Exception
	 */
	private void saveOrderSetle(OrderSetleVO orderSetle) throws Exception {
		String tempKey  = NumberUtil.numberGen(4)+System.currentTimeMillis();
		orderSetle.setTempKey(tempKey);
		
		Boolean isSbs = false;
		if(orderSetle.getIsSbs()==true && orderSetle.getIsSbs()!=null)isSbs = true;
		//?????? ?????? ?????????????????? ?????????
		if(isSbs){
			orderSetle.setSetleSttusCode("R");
			orderSetle.setSetleResultMssage(null);
			orderSetle.setSetleResultCode(null);
			orderSetle.setSetleCardAmount(null);
			orderSetle.setSetlePoint(null);
			orderSetle.setEzwSetleConfmNo(null);
			orderSetle.setIniSetleConfmNo(null);
			orderSetle.setSetlePnttm(null);
			orderSetle.setSetleResultTyCode(null);
		}
		orderSetleMapper.insertSelectOrder(orderSetle);
		OrderSetleVO newOrderSetle = orderSetleMapper.selectOrderSetle(orderSetle);
		
		HashMap<String, Object> map =new HashMap<>();
		
		map.put("orderSetleNo", newOrderSetle.getOrderSetleNo());
		map.put("orderNo", orderSetle.getSearchOrderNo());
		
		if(isSbs){
			map.put("orderReqSttusCode", 'W');
			map.put("dscntPc", 0);
			map.put("orderInfo",orderSetle.getSeOrderInfo());
		}else{
			map.put("orderReqSttusCode", 'O');
			map.put("dscntPc", orderSetle.getDscntPc());
			map.put("orderInfo",orderSetle.getFirOrderInfo());
		}
		
		orderSetleMapper.insertNextSTN_ORDER_DLVY(map);
		
		//????????? ????????????????????????
		if(isSbs){
			OrderDlvyVO orderDlvy = new OrderDlvyVO();
			orderDlvy.setOrderNo(orderSetle.getSearchOrderNo());
			orderDlvyMapper.updateSbsOrderDlvy(orderDlvy);
		}
		
		
		//?????? ????????? ??????
		if(!"R".equals(orderSetle.getSetleSttusCode())){
			orderSetleMapper.updateExcclcPrarnde(newOrderSetle);
		}
	}
	
	/*
	 * ????????? ????????? ??????
	 *@param infoMap
	 *@param orderList
	 *@throws Exception
	 */
	private HashMap<String, Object> ezwelPointPay(HashMap<String, Object> infoMap, List<OrderVO> orderList) throws Exception{
		
		EzwelFunc ezwelFunc = new EzwelFunc();
		HashMap<String, Object> resultMap = new HashMap<>();
		GoodsVO goods = new GoodsVO();
		SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd");
		SimpleDateFormat hourFormat = new SimpleDateFormat("hh:mm:ss");
		
		infoMap.put("orderNm", infoMap.get("userName"));
		infoMap.put("orderEmail", infoMap.get("userEmail"));
		
		//???????????? ???????????????
		ArrayDeque<String> ezCodeList = new ArrayDeque<String>();
		ArrayDeque<java.math.BigDecimal> ezPointQue= new ArrayDeque<java.math.BigDecimal>();
		ArrayDeque<java.math.BigDecimal> ezPointStored= new ArrayDeque<java.math.BigDecimal>();
		if(orderList.size()>1){
			infoMap.put("goodsNm", "???????????????"+orderList.size()+"?????? ??????");
			for(OrderVO od : orderList){
				//????????? ????????? ??????
				java.math.BigDecimal ezwelPoint = ezwelFunc.ezwelPointSearch((String)infoMap.get("userKey"), (String)infoMap.get("clientCd"));
				goods.setGoodsId(od.getGoodsId());
				goods=goodsMapper.selectGoods(goods);
				infoMap.put("useMileage",0);
				infoMap.put("useSpecialPoint", 0);
				infoMap.put("goodsNm", goods.getGoodsNm());
				infoMap.put("unitCost", goods.getGoodsPc());
				infoMap.put("buyPrice", goods.getGoodsSplpc());
				infoMap.put("orderCount",od.getOrderCo());
				infoMap.put("orderTotal",od.getTotAmount().add(od.getDscntAmount()));
				if(infoMap.get("payMethod").equals("both")){
					//???????????? ??????????????? ??????
					if(ezwelPoint.compareTo(java.math.BigDecimal.ZERO)==1){
						infoMap.remove("useEzwelPoint");
						if(ezwelPoint.compareTo(od.getTotAmount().add(od.getDscntAmount()))==1)
							infoMap.put("useEzwelPoint",od.getTotAmount().add(od.getDscntAmount()));
						else{
							infoMap.put("useEzwelPoint",ezwelPoint);
						}
						infoMap.put("usePoint", infoMap.get("useEzwelPoint"));
						infoMap.put("payMoney", infoMap.get("useEzwelPoint"));
					}else{
						infoMap.remove("useEzwelPoint");
						infoMap.put("useEzwelPoint",java.math.BigDecimal.ZERO);
						infoMap.put("usePoint", 0);
						infoMap.put("payMoney", 0);
					}
					ezPointQue.add((BigDecimal)infoMap.get("useEzwelPoint"));
					ezPointStored.add((BigDecimal)infoMap.get("useEzwelPoint"));

				}else if(infoMap.get("payMethod").equals("point")){
					infoMap.put("usePoint", od.getTotAmount().add(od.getDscntAmount()));
					infoMap.put("payMoney", od.getTotAmount().add(od.getDscntAmount()));
				}else if((Boolean)infoMap.get("isCard")==true){
					infoMap.put("usePoint", java.math.BigDecimal.ZERO);
					infoMap.put("payMoney", java.math.BigDecimal.ZERO);
				}
				infoMap.put("orderDd", dayFormat.format(od.getOrderPnttm()));
				infoMap.put("orderTm", hourFormat.format(od.getOrderPnttm()));
				infoMap.put("aspOrderNum",od.getOrderNo());
				/*infoMap.put("aspOrderNum",infoMap.get("orderGroupNo"));*/
				resultMap=ezwelFunc.ezwelPointUse(infoMap);
				ezCodeList.add((String)resultMap.get("ezCode"));
				resultMap.put("ezCodeList",ezCodeList);
				resultMap.put("ezCodes", true);
				resultMap.put("ezPointQue",ezPointQue);
				resultMap.put("ezPointStored",ezPointStored);
			}
			//??????????????? ????????? ???
		}else{
				OrderVO od = orderList.get(0);
				goods.setGoodsId(od.getGoodsId());
				goods=goodsMapper.selectGoods(goods);
				infoMap.put("useMileage",0);
				infoMap.put("useSpecialPoint", 0);
				infoMap.put("goodsNm", goods.getGoodsNm());
				infoMap.put("unitCost", goods.getGoodsPc());
				infoMap.put("buyPrice", goods.getGoodsSplpc());
				infoMap.put("orderCount",orderList.get(0).getOrderCo());
				infoMap.put("orderTotal",orderList.get(0).getTotAmount().add(od.getDscntAmount()));
				if(infoMap.get("payMethod").equals("both")){
					infoMap.put("usePoint", infoMap.get("useEzwelPoint"));
					infoMap.put("payMoney", infoMap.get("useEzwelPoint"));
				}else if(infoMap.get("payMethod").equals("point")){
					infoMap.put("usePoint", od.getTotAmount().add(od.getDscntAmount()));
					infoMap.put("payMoney", od.getTotAmount().add(od.getDscntAmount()));
				}else if((Boolean)infoMap.get("isCard")==true){
					infoMap.put("usePoint", java.math.BigDecimal.ZERO);
					infoMap.put("payMoney", java.math.BigDecimal.ZERO);
				}
				infoMap.put("orderDd", dayFormat.format(od.getOrderPnttm()));
				infoMap.put("orderTm", hourFormat.format(od.getOrderPnttm()));
				infoMap.put("aspOrderNum",od.getOrderNo());
				/*infoMap.put("aspOrderNum", infoMap.get("orderGroupNo"));*/
				resultMap=ezwelFunc.ezwelPointUse(infoMap);
				resultMap.put("ezCode", (String)resultMap.get("ezCode"));
				resultMap.put("ezCodes", false);
		}
		return resultMap;
	}
	/*
	 * ?????? ??????
	 *@param infoMap
	 *@param orderList
	 *@throws Exception
	 */
	private HashMap<String, Object> cardPay(HashMap<String, Object> infoMap, List<OrderVO> orderList) throws Exception{
		
		HashMap<String, Object> resultMap = new HashMap<>();
		Biling billing = new Biling();
		GoodsVO goods = new GoodsVO();
		ArrayDeque<java.math.BigDecimal> ezPointQue= new ArrayDeque<java.math.BigDecimal>();
		//???????????? ???????????????
		if(orderList.size()>1){
			infoMap.put("goodsNm", "???????????????"+orderList.size()+"?????? ??????");
			
			java.math.BigDecimal totPc = new BigDecimal(0);
			
			for(OrderVO od : orderList){
				if(infoMap.get("ezPointQue")!=null){
					ezPointQue= (ArrayDeque<java.math.BigDecimal>) infoMap.get("ezPointQue");
					totPc = totPc.add(od.getDscntAmount().add(od.getTotAmount()).subtract((BigDecimal)ezPointQue.poll()));
				}else{
					totPc = totPc.add(od.getDscntAmount().add(od.getTotAmount()));
				}
			}
			infoMap.put("goodsPc",totPc);
			//??????????????? ????????? ???
		}else{
				OrderVO od = orderList.get(0);
				goods.setGoodsId(od.getGoodsId());
				goods=goodsMapper.selectGoods(goods);
				infoMap.put("goodsNm", goods.getGoodsNm());
				if(infoMap.get("useEzwelPoint")!=null){
					infoMap.put("goodsPc",od.getDscntAmount().add(od.getTotAmount()).subtract((BigDecimal)infoMap.get("useEzwelPoint")));
				}else{
					infoMap.put("goodsPc", od.getDscntAmount().add(od.getTotAmount()));
				}
		}
		//??????(??????????????? ???????????? ??? ??????)
		resultMap = billing.biling(infoMap);
		return resultMap;
	}

	/**
	 * ???????????? ??????
	 *
	 * @throws Exception
	 */
	@Override
	public void deleteOrderGroup(OrderVO order) throws Exception {
		List<OrderVO> orderList = orderMapper.selectMyOrderList(order);
		//??????????????????
		for (OrderVO od : orderList) {
			OrderItemVO odItem = new OrderItemVO();
			odItem.setOrderNo(od.getOrderNo());
			orderMapper.deleteOrderItem(odItem);
		}
		//????????????
		orderMapper.deleteOrder(order);
		orderMapper.deleteOrderGroup(order);
	}
	
	@Override
	public void updateOrder(OrderVO order) throws Exception {
		
		orderMapper.updateOrder(order);
		
	}
	/**
	 * ?????? ??????
	 */
	@Override
	public void updateSbsOrder(OrderVO order) throws Exception {
		
		OrderVO searchVO = new OrderVO();
		searchVO.setOrderNo(order.getOrderNo());
		searchVO = orderMapper.selectOrder(searchVO);
		
		GoodsItemVO goodsItem = new GoodsItemVO();
		OrderItemVO orderItem = new OrderItemVO(); 
		StringBuilder orderInfo = new StringBuilder();
		java.math.BigDecimal dscntPc = new BigDecimal(0);
		String nextSetleDe=null;
		
			
		if(order.getSbscrptMtCycle()!=null || order.getSbscrptDlvyDay()!=null ||order.getSbscrptWeekCycle()!=null || order.getSbscrptDlvyDay()!=null){
			nextSetleDe = nextSbsDate(order);
		}
		order.setNextSetlede(nextSetleDe);
		orderItem.setOrderNo(order.getOrderNo());
		//?????? ?????? ?????? ??? ??????
		if(order.getOrderItemIdList()!=null && order.getOrderItemIdList().size()>0 && StringUtils.isEmpty(order.getDlvyAdres())){
			orderMapper.deleteOrderItem(orderItem);
			for(int i=0;i<order.getOrderItemIdList().size();i++){
				if(StringUtils.isNotEmpty(order.getOrderItemIdList().get(i))){
					java.math.BigDecimal orderItemNo = orderItemIdGnrService.getNextBigDecimalId();
					goodsItem.setGitemId(order.getOrderItemIdList().get(i));
					goodsItem=goodsItemMapper.selectGoodsItem(goodsItem);
					orderItem.setOitemNo(orderItemNo);
					orderItem.setOrderNo(order.getOrderNo());
					orderItem.setGitemId(order.getOrderItemIdList().get(i));
					orderItem.setGitemNm(goodsItem.getGitemNm());
					orderItem.setGitemPc(goodsItem.getGitemPc());
					orderItem.setGistemSeCode(goodsItem.getGitemSeCode());
					orderMapper.insertOrderItem(orderItem);
					String sym = "+";
					if(orderItem.getGitemPc().compareTo(java.math.BigDecimal.ZERO)==-1){
						sym = "";
						dscntPc = dscntPc.add(goodsItem.getGitemPc());
					}
					if("D".equals(orderItem.getGistemSeCode())){
						orderInfo.append("????????????:"+orderItem.getGitemNm()+"("+sym+orderItem.getGitemPc()+")");
					}else if("A".equals(orderItem.getGistemSeCode())){
						orderInfo.append("????????????:"+orderItem.getGitemNm()+"("+sym+orderItem.getGitemPc()+")");
					}else if("F".equals(orderItem.getGistemSeCode())){
						orderInfo.append("???????????????:"+orderItem.getGitemNm()+"("+sym+orderItem.getGitemPc()+")");
					}
				}
			}
		}
		//?????? ?????? ?????? ??????
		if("Y".equals(order.getCompnoDscntUseAt())){
			GoodsVO goods = new GoodsVO();
			goods.setGoodsId(searchVO.getGoodsId());
			goods = goodsMapper.selectGoods(goods);
			dscntPc = dscntPc.add(goods.getCompnoDscntPc().multiply(new BigDecimal(order.getOrderCo())));
		}
		
		if(dscntPc.compareTo(new BigDecimal(0))==0){
			order.setDscntAmount(new BigDecimal(0));
		}else{
			order.setDscntAmount(dscntPc);
		}
		
		//??????????????????
		orderMapper.updateSbsOrder(order);
		
		/*List<?> orderDlvyList =orderMapper.selectOrderDlvyList(order);*/
		OrderDlvyVO dlvy = orderMapper.selectRecetOrderDlvy(order);
		//?????? ?????? ?????? ??????
		OrderDlvyVO orderDlvy = new OrderDlvyVO();
		if(dscntPc.compareTo(new BigDecimal(0))==0){
			orderDlvy.setDscntAmount(dscntPc.multiply(new BigDecimal(-1)));
		}else{
			orderDlvy.setDscntAmount(new BigDecimal(0));
		}
		orderDlvy.setOrderInfo(orderInfo.toString());
		orderDlvy.setOrderDlvyNo(dlvy.getOrderDlvyNo());
		orderDlvy.setOrderCo(order.getOrderCo());
		orderDlvy.setDlvyAdres(order.getDlvyAdres());
		orderDlvy.setDlvyAdresDetail(order.getDlvyAdresDetail());
		orderDlvy.setDlvyUserNm(order.getDlvyUserNm());
		orderDlvy.setDlvyMssage(order.getDlvyMssage());
		orderDlvy.setDlvyZip(order.getDlvyZip());
		orderDlvy.setTelno(order.getRecptrTelno());
		orderDlvy.setLastUpdusrId(order.getLastUpdusrId());
		if(order.getSbscrptMtCycle()!=null || order.getSbscrptDlvyDay()!=null){
			int sbscrptMtCycle =  order.getSbscrptMtCycle()!=null?order.getSbscrptMtCycle():null;
			int sbscrptDlvyDay =  order.getSbscrptDlvyDay()!=null?order.getSbscrptDlvyDay():null;
			orderDlvy.setSbscrptMtCycle(sbscrptMtCycle);
			orderDlvy.setSbscrptDlvyDay(sbscrptDlvyDay);
		}else if(order.getSbscrptWeekCycle()!=null || order.getSbscrptDlvyDay()!=null){
			int sbscrptWeekCycle = order.getSbscrptWeekCycle()!=null?order.getSbscrptWeekCycle():null;
			int sbscrptDlvyWd = order.getSbscrptDlvyWd()!=null?order.getSbscrptDlvyWd():null;
			orderDlvy.setSbscrptWeekCycle(sbscrptWeekCycle);
			orderDlvy.setSbscrptDlvyWd(sbscrptDlvyWd);
		}
		orderDlvyMapper.updateOrderDlvy(orderDlvy);
		
		//?????? ??????????????? ??????
		OrderSetleVO orderSetle = new OrderSetleVO();
		orderSetle.setOrderSetleNo(dlvy.getOrderSetleNo());
		orderSetle.setSetleTotAmount(new BigDecimal(-1));
		if(nextSetleDe!=null){
			orderSetle.setSetlePrarnde(nextSetleDe);
		}
		orderSetleMapper.updateOrderSetle(orderSetle);
	}
	
	
	/**
	 * ??????????????? > ???????????? ?????? ?????? ??????
	 */

	@Override
	public int selectSubscribeNowListCnt(OrderVO searchVO) {
		return orderMapper.selectSubscribeNowListCnt(searchVO);
	}

	/**
	 * ??????????????? > ???????????? ?????? ??????
	 */

	@Override
	public List<?> selectSubscribeNowList(OrderVO searchVO) {
		return orderMapper.selectSubscribeNowList(searchVO);
	}

	@Override
	public OrderDlvyVO selectRecetOrderDlvy(OrderVO searchVO) {
		return orderMapper.selectRecetOrderDlvy(searchVO);
	}
	
	/**
	 * ?????? 1??? ?????? ??????
	 * @throws Exception 
	 */
	@Override
	public OrderVO selectOrder(OrderVO searchVO) throws Exception {
		searchVO=orderMapper.selectOrder(searchVO);
		if(searchVO!=null){
			if("CPN".equals(searchVO.getOrderKndCode())){
				GoodsCouponVO goodsCoupon = new GoodsCouponVO();
				goodsCoupon.setOrderNo(searchVO.getOrderNo());
				List<GoodsCouponVO> orderCouponList = goodsCouponMapper.selectGoodsCouponList(goodsCoupon);
				searchVO.setOrderCouponList(orderCouponList);
			}
		}
		return searchVO;
	}
	
	@Override
	public void deleteOrderItem(OrderItemVO searchVO) {
		orderMapper.deleteOrderItem(searchVO);
	}

	@Override
	public EgovMap selectModOrder(OrderVO searchVO) throws Exception {
		EgovMap order = orderMapper.selectModOrder(searchVO);
		//order ????????? ????????? 
		/*OrderItemVO orderItem = new OrderItemVO();*/
		List<OrderItemVO> orderItemList = (List<OrderItemVO>) orderMapper.selectOrderItemList(searchVO);
		for(OrderItemVO o : orderItemList){
			if(o.getGistemSeCode().equals("D")){
			order.put("ditem", o);
			}else if(o.getGistemSeCode().equals("A")){
			order.put("aitem", o);
			}else if(o.getGistemSeCode().equals("F")){
			order.put("fitem", o);
			}
		}
		
		//?????? ????????????
		GoodsVO goods = new GoodsVO();
		goods.setGoodsId((String)order.get("goodsId"));
		goods=goodsMapper.selectGoods(goods);
		order.put("goods", goods);
		
		//?????? ????????? ?????????
		GoodsItemVO goodsItem = new GoodsItemVO();
		goodsItem.setGoodsId((String)order.get("goodsId"));
		goodsItem.setGitemSeCode("D");
		List<GoodsItemVO> doptList = goodsItemMapper.selectGoodsItemList(goodsItem);
		order.put("dopt", doptList);
		goodsItem.setGitemSeCode("A");
		List<GoodsItemVO> aoptList = goodsItemMapper.selectGoodsItemList(goodsItem);
		order.put("aopt", aoptList);
		goodsItem.setGitemSeCode("F");
		List<GoodsItemVO> foptList = goodsItemMapper.selectGoodsItemList(goodsItem);
		order.put("fopt", foptList);
		return order;
	}
	/**
	 * ?????? 1?????? ?????? ??????
	 */
	@Override
	public List<?> selectOrderItemList(OrderVO searchVO) {
		return orderMapper.selectOrderItemList(searchVO);
	}
	
	/**
	 * ?????? 1?????? ??????????????????
	 */
	@Override
	public List<?> selectOrderQItemList(OrderVO searchVO) {
		return orderMapper.selectOrderQItemList(searchVO);
	}
	
	/**
	 * ?????? 1?????? ?????? ??????
	 */
	@Override
	public List<?> selectOrderDlvyList(OrderVO searchVO) {
		return orderMapper.selectOrderDlvyList(searchVO);
	}

	
	/*
	 * ?????? ?????? ?????? ??????
	 * @param orderVO
	 * @return
	 * throws Exception
	 */
	private String nextSbsDate(OrderVO order) throws Exception {
		/*order=orderMapper.selectOrder(order);*/
		Calendar cal = Calendar.getInstance();
		cal.setTime(order.getOrderPnttm());//????????????
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
		
		if(order.getSbscrptCycleSeCode().equalsIgnoreCase("WEEK")){
			int curWeek = cal.get(Calendar.DAY_OF_WEEK);	//?????? ??????(???)
			int sbsWkCycle = order.getSbscrptWeekCycle();	//?????? ??????(??????)
			int sbsWkWd = order.getSbscrptDlvyWd(); 	//????????????
			cal.add(Calendar.WEEK_OF_MONTH, sbsWkCycle);
			cal.add(Calendar.DAY_OF_WEEK, sbsWkWd-curWeek);
			
			
			Calendar cal2 = Calendar.getInstance();
			Calendar cal1 = Calendar.getInstance();
			
			cal2.setTime(cal.getTime());
			cal1.setTime(order.getOrderPnttm());
			
			cal2.add(Calendar.DATE, -cal1.get(Calendar.DATE));
			int day = Integer.valueOf(dateFormat.format(cal2.getTime()).substring(6,8));
			
			if(day < 4){
				cal.add(Calendar.WEEK_OF_MONTH, 1);
			}
			
		}else if(order.getSbscrptCycleSeCode().equalsIgnoreCase("MONTH")){
			int curDay = cal.get(Calendar.DAY_OF_MONTH);	
			int sbsMtCycle = order.getSbscrptMtCycle();	//?????? ?????? (???)
			int sbsMtdy = order.getSbscrptDlvyDay();	//?????? ??????
			cal.add(Calendar.MONTH, sbsMtCycle);
            /*cal.add(Calendar.DAY_OF_MONTH, sbsMtdy-curDay);*/
           
           
			Calendar cal2 = Calendar.getInstance();
			Calendar cal1 = Calendar.getInstance();
			
			cal2.setTime(cal.getTime());
			cal1.setTime(order.getOrderPnttm());
			
			cal2.add(Calendar.DAY_OF_MONTH, -cal1.get(Calendar.DAY_OF_MONTH));
			
			int day = Integer.valueOf(dateFormat.format(cal2.getTime()).substring(6,8));
			
			if(day < 15){
				cal.add(Calendar.MONTH, 1);
			}
			int actualMaximum = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
			if(sbsMtdy > actualMaximum) {
                sbsMtdy = actualMaximum;
			}           
			cal.set(Calendar.DAY_OF_MONTH, sbsMtdy);
		}
		return dateFormat.format(cal.getTime());
	}
	
	/**
	 * ?????? ??????, ?????? ?????? ?????? ??????
	 */
	@Override
	@Transactional
	public void cancelOrder(OrderVO searchVO) {
		searchVO.setOrderSttusCode("ST99");
		orderMapper.updateOrderStatus(searchVO);
		
		OrderDlvyVO searchDlvy = new OrderDlvyVO();
		searchDlvy.setOrderNo(searchVO.getOrderNo());
		searchDlvy.setOrderReqSttusCode("C");
		searchDlvy.setReqTyCode("C01");
		
		System.out.println("=============================" + searchDlvy.getOrderNo());

		orderDlvyMapper.updateOrderDlvyStatus(searchDlvy);

		OrderReqHistVO vo = new OrderReqHistVO();
		vo.setOrderDlvyNo(orderDlvyMapper.selectMaxOrderDlvyNoByOrderNo(searchDlvy));
		vo.setReqTyCode(searchDlvy.getReqTyCode());
		
		orderReqHistMapper.insertOrderReqHist(vo); // ?????? ??????

	}

	/**
	 * ??????????????? ??????/?????? ??????
	 */
	
	@Override
	public List<?> selectMyRefundList(OrderVO searchVO) {
		return orderDlvyMapper.selectMyRefundList(searchVO);
	}
	
	/**
	 * ??????????????? ??????/?????? ?????? ??????
	 */
	@Override
	public int selectMyRefundListCnt(OrderVO searchVO) {
		return orderDlvyMapper.selectMyRefundListCnt(searchVO);
	}

	/**
	 * ??????/?????? ??????
	 * @throws Exception 
	 */
	@Override
	@Transactional
	public void refundOrder(OrderDlvyVO dlvyVO, QainfoVO qainfo) throws Exception {
	
		java.math.BigDecimal orderDlvyNo = orderDlvyMapper.selectMaxOrderDlvyNoByOrderNo(dlvyVO);
		//????????????, ???????????? ??????
		dlvyVO.setOrderDlvyNo(orderDlvyNo);
		orderDlvyMapper.updateDlvySttusCode(dlvyVO);
		orderDlvyMapper.updateOrderDlvyStatus(dlvyVO);
		// ??????/?????? ?????? ??????
		String qaId = qainfoIdGnrService.getNextStringId();
		qainfo.setQaId(qaId);
		qainfo.setWritngDe(CommonUtils.getCurrentDateTime());
		qainfoMapper.insertQainfo(qainfo);
		// ?????? ??????
		OrderReqHistVO histVO = new OrderReqHistVO();
		histVO.setOrderDlvyNo(orderDlvyNo);
		histVO.setReqTyCode(dlvyVO.getReqTyCode());
		histVO.setReasonCn(qainfo.getQestnCn());
		orderReqHistMapper.insertOrderReqHist(histVO);
	}
	/**
	 * ??????????????????
	 */
	@Override
	@Transactional
	public void stopSubscribeRequest(OrderVO searchVO) {
		searchVO.setOrderSttusCode("ST01");
		orderMapper.updateOrderStatus(searchVO);
		OrderDlvyVO dlvyVO = new OrderDlvyVO();
		dlvyVO.setOrderReqSttusCode("T");
		dlvyVO.setReqTyCode("T01");
		dlvyVO.setOrderNo(searchVO.getOrderNo());
		orderDlvyMapper.updateOrderDlvyStatusReady(dlvyVO);
		
		// TODO ?????? ???????????? insert
	}
	/**
	 * ???????????? ??????
	 */
	@Override
	@Transactional
	public void stopSubscribeConfirm(OrderVO searchVO) {
		searchVO.setOrderSttusCode("ST04");
		orderMapper.updateOrderStatus(searchVO);
		OrderDlvyVO dlvyVO = new OrderDlvyVO();
		dlvyVO.setOrderReqSttusCode("T");
		dlvyVO.setReqTyCode("T02");
		dlvyVO.setOrderNo(searchVO.getOrderNo());
		orderDlvyMapper.updateOrderDlvyStatusReady(dlvyVO);
		orderSetleMapper.stopSubscribeConfirm(searchVO);
		
		// TODO ?????? ???????????? insert
	}
	/**
	 * ???????????? ??????
	 */
	@Override
	public void stopSubscribeCancel(OrderVO searchVO) {
		searchVO.setOrderSttusCode("ST02");
		orderMapper.updateOrderStatus(searchVO);
		OrderDlvyVO dlvyVO = new OrderDlvyVO();
		dlvyVO.setOrderReqSttusCode("W");
		dlvyVO.setReqTyCode("T03");
		
		dlvyVO.setOrderNo(searchVO.getOrderNo());
		orderDlvyMapper.updateOrderDlvyStatusReady(dlvyVO);
		//orderSetleMapper.stopSubscribeCancel(searchVO);
		
		OrderReqHistVO vo = new OrderReqHistVO();	
		vo.setOrderDlvyNo(orderDlvyMapper.selectMaxOrderDlvyNoByOrderNo(dlvyVO));	
		vo.setReqTyCode("T03");	
			
		orderReqHistMapper.insertOrderReqHist(vo); // ?????? ??????
		
	}
	/**
	 * ?????? ?????? ??????
	 */
	@Override
	public int selectSbsCnt(OrderVO searchVO) {
		
		return orderMapper.selectSbsCnt(searchVO);
	}
	/**
	 *1??? ?????? ?????? ???????????? 
	 */
	@Override
	public int selectExprnCnt(OrderVO searchVO) {
		return orderMapper.selectExprnCnt(searchVO);
	}
	/**
	 * ????????? ?????? 
	 */
	@Override
	public int selectEvtGoodsOrderCnt(OrderVO searchVO) {
		return orderMapper.selectEvtGoodsOrderCnt(searchVO);
	}
	
	
}
