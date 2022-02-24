package modoo.module.shop.hdry.service.impl;

import java.util.List;

import egovframework.rte.psl.dataaccess.mapper.Mapper;
import modoo.module.shop.goods.brand.service.GoodsBrandVO;
import modoo.module.shop.hdry.service.HdryCmpnyVO;

@Mapper("hdryCmpnyMapper")
public interface HdryCmpnyMapper {

	/**
	 * 택배사목록
	 * @param searchVO
	 * @return
	 * @throws Exception
	 */
	List<HdryCmpnyVO> selectHdryCmpnyList(HdryCmpnyVO searchVO) throws Exception;
	
	/**
	 * 택배사목록 카운트
	 * @param searchVO
	 * @return
	 * @throws Exception
	 */
	int selectHdryCmpnyListCnt(HdryCmpnyVO searchVO) throws Exception;
	
	/**
	 * 택배사저장
	 * @param hdryCmpny
	 * @throws Exception
	 */
	void insertHdryCmpny(HdryCmpnyVO hdryCmpny) throws Exception;
	
	/**
	 * 택배사상세
	 * @param hdryCmpny
	 * @return
	 * @throws Exception
	 */
	HdryCmpnyVO selectHdryCmpny(HdryCmpnyVO hdryCmpny) throws Exception;
	
	/**
	 * 택배사수정
	 * @param hdryCmpny
	 * @throws Exception
	 */
	void updateHdryCmpny(HdryCmpnyVO hdryCmpny) throws Exception;
	
	/**
	 * 택배사삭제
	 * @param hdryCmpny
	 * @throws Exception
	 */
	void deleteHdryCmpny(HdryCmpnyVO hdryCmpny) throws Exception;
	
	/**
	 * 택배사명 체크 카운트
	 * @param hdryCmpny
	 * @return
	 * @throws Exception
	 */
	int selectHdryCmpnyCheckCnt(HdryCmpnyVO hdryCmpny) throws Exception;
	/**
	 * 업체별 택배사 목록 조회
	 * @param hdryCmpny
	 * @return
	 */
	List<HdryCmpnyVO> selectGoodsHdryList(HdryCmpnyVO hdryCmpny);
}
