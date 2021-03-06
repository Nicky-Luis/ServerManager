package com.dbumama.market.web.admin.qiandao.controller;

import com.dbumama.market.model.AuthUser;
import com.dbumama.market.model.Qiandao;
import com.dbumama.market.model.QiandaoConfig;
import com.dbumama.market.service.api.qiandao.QiandaoException;
import com.dbumama.market.service.api.qiandao.QiandaoResultDto;
import com.dbumama.market.service.api.qiandao.QiandaoService;
import com.dbumama.market.service.utils.DateTimeUtil;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.AdminBaseController;
import com.dbumama.market.web.core.plugin.spring.Inject;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * wjun_java@163.com
 * 2016年7月2日
 */
@RouteBind(path = "qiandao", viewPath="qiandao")
public class QiandaoController extends AdminBaseController<Qiandao>{

	@Inject.BY_NAME
	private QiandaoService qiandaoService;
	
	public void index(){
		List<AuthUser> authUsers = AuthUser.dao.find("select * from " + AuthUser.table + " where seller_id=? and active=1 ", getSellerId());
		setAttr("authUsers", authUsers);
		render("qd_index.html");
	}
	
	public void list(){
		List<Object> params = new ArrayList<Object>();
		params.add(getSellerId());
		StringBuilder sqlBuilder = new StringBuilder("from " + Qiandao.table + " where seller_id = ? and active=1 ");
		
		if(StrKit.notBlank(getPara("qname"))){
			sqlBuilder.append(" and qiandao_name like ? ");
			params.add("%" +getPara("qname")+ "%");
		}
		
		sqlBuilder.append(" order by updated desc ");
		Object [] o = new Object[params.size()];
		for(int i=0;i<params.size();i++){
			o [i] = params.get(i); 
		}
		Page<Qiandao> qiandaoPage = Qiandao.dao.paginate(getPageNo(), getPageSize(), 
				"select * ", 
				sqlBuilder.toString(), 
				o);
		
		List<QiandaoResultDto> qiandaoResults = new ArrayList<QiandaoResultDto>();
		for(Qiandao q : qiandaoPage.getList()){
			QiandaoResultDto qiandaoResultDto = new QiandaoResultDto();
			qiandaoResultDto.setId(q.getId());
			qiandaoResultDto.setQiandaoName(q.getQiandaoName());
			qiandaoResultDto.setStartDate(q.getStartDate());
			qiandaoResultDto.setEndDate(q.getEndDate());
			qiandaoResultDto.setQrCode(q.getQrCode());
			if(getUsedAuthUser() !=null){
				qiandaoResultDto.setWirlessUrl("http://"+getUsedAuthUser().getAppId()+".dbumama.com/qiandao/?qiandaoId="+q.getId());
			}
			if(DateTimeUtil.nowDate().after(q.getEndDate())){
				qiandaoResultDto.setStatus("已结束");
			}
			if(DateTimeUtil.nowDate().before(q.getEndDate())){
				qiandaoResultDto.setStatus("未开始");
			}
			if(DateTimeUtil.nowDate().after(q.getStartDate()) && DateTimeUtil.nowDate().before(q.getEndDate())){
				qiandaoResultDto.setStatus("进行中");
			}
			qiandaoResults.add(qiandaoResultDto);
		}
		
		Page<QiandaoResultDto> qresultPage = new Page<QiandaoResultDto>(qiandaoResults, getPageNo(), getPageSize(), qiandaoPage.getTotalPage(), qiandaoPage.getTotalRow());
		rendSuccessJson(qresultPage);
	}
	
	public void save(){
		Qiandao qiandao = null;
		try {
			qiandao = getModel();
		} catch (Exception e) {
			e.printStackTrace();
			rendFailedJson("获取签到信息失败！");
			return;
		}
		
		if(qiandao == null){
			rendFailedJson("获取签到信息失败！");
			return;
		}
		
		if(qiandao.getId() == null){
			qiandao.setSellerId(getSellerId());
			try {
				Qiandao qiandaoDto = qiandaoService.doSave(qiandao, getPara("items"), getSellerUser());
				rendSuccessJson(qiandaoDto);
			} catch (QiandaoException e) {
				rendFailedJson(e.getMessage());
			}
		}else{
			try {
				Qiandao qiandaoDto = qiandaoService.doUpdate(qiandao, getPara("items"), getSellerUser());
				rendSuccessJson(qiandaoDto);
			} catch (QiandaoException e) {
				rendFailedJson(e.getMessage());
			}
		}
	}
	
	public void del(){
		String ids = getPara("ids");
		for(String id : ids.split("-")){
			Qiandao qiandao = Qiandao.dao.findById(id);
			qiandao.setActive(0);
			qiandao.update();
		}
		rendSuccessJson("操作成功！");
	}
	
	public void add(){
		if(!StrKit.isBlank(getPara(0))){
			Qiandao qiandao = Qiandao.dao.findById(getPara(0));
			if(qiandao != null){
				setAttr("qiandao", qiandao);
				List<QiandaoConfig> actqiandaos = QiandaoConfig.dao.find(" select * from " + QiandaoConfig.table + " where qiandao_id=? and active=1 ", getPara(0));
				List<QiandaoConfig> joinQiandaoConfigs = new ArrayList<QiandaoConfig>();
				List<QiandaoConfig> addQiandaoConfigs = new ArrayList<QiandaoConfig>();
				List<QiandaoConfig> assignQiandaoConfigs = new ArrayList<QiandaoConfig>();
				for(QiandaoConfig aq : actqiandaos){
					if(aq.getInt("sign_type") == 1)
						joinQiandaoConfigs.add(aq);
					else if(aq.getInt("sign_type") == 2)
						addQiandaoConfigs.add(aq);
					else 
						assignQiandaoConfigs.add(aq);
				}
				setAttr("joinItems", joinQiandaoConfigs);
				setAttr("addItems", addQiandaoConfigs);	
				setAttr("assignItems", assignQiandaoConfigs);	
			}
		}
		//获取上次签到活动的结束时间
		Record record = Db.findFirst("select t.end_date from t_qiandao t "
				+ "where t.active=1 and t.seller_id=? "
				+ "ORDER BY t.end_date desc LIMIT 1", getSellerId());
		if(record != null){
			setAttr("lastDate", DateTimeUtil.getNextDateStringAddDay(DateTimeUtil.FORMAT_YYYY_MM_DD.format(record.getDate("end_date")), 1));			
		}
		render("qd_setting.html");
	}
	
	public void checktime(){
		//修改不做时间检查
		if(StrKit.notBlank(getPara("id"))){
			rendSuccessJson();
			return;
		}
		
		Date startDate = getParaToDate("start_date");
		Date endDate = getParaToDate("end_date");
		
		if(startDate == null || endDate == null){
			rendFailedJson("开始时间或结束时间为空");
			return;
		}
		
		if(endDate.before(startDate)){
			rendFailedJson("<p style='color:red;'>警告：结束时间要大于开始时间，请修改时间！</p>");
			return;
		}
		
		if(DateTimeUtil.compareDay(endDate, startDate)<7){
			rendFailedJson("<p style='color:red;'>警告：签到时间周期不能少于7天，建议设置为1个月！</p>");
			return;
		}
		
		//检查该时间范围内是否有签到活动
		Record record = Db.findFirst("select t.end_date from t_qiandao t "
				+ "where t.active=1 and t.seller_id=? "
				+ "ORDER BY t.end_date desc LIMIT 1", getSellerId());
		if(record ==null || record.getDate("end_date") == null){
			rendSuccessJson();
			return;
		}
		
		if(startDate.after(record.getDate("end_date"))){
			rendSuccessJson();
			return;
		}
		
		rendFailedJson("<p style='color:red;'>警告：检测到签到时间冲突！</p><p>请修改当前签到开始时间在<font color='red'>["+DateTimeUtil.toDateString(record.getDate("end_date"))+"]</font>后！</p>");
		
	}
	
	public void stat(){
		render("qd_stat.html");
	}
	
	public void statlist(){
		final String select = " select q.qiandao_name, bu.nickname, qu.qiandao_count,qu.c_qiandao_count, qu.sign_date ";
		final String sqlExceptSelect = "from t_qiandao_user qu " 
					+ " LEFT JOIN t_buyer_user bu ON qu.buyer_id = bu.id "
					+ " LEFT JOIN t_qiandao q ON qu.qiandao_id = q.id "
					+ " WHERE q.seller_id = ? and q.active=1";
		Page<Record> pages = Db.paginate(getPageNo(), getPageSize(), select, sqlExceptSelect, getSellerId());
		rendSuccessJson(pages);
	}
	
	/* (non-Javadoc)
	 * @see com.dbumama.market.web.common.controller.AdminBaseController#getModelClass()
	 */
	@Override
	protected Class<Qiandao> getModelClass() {
		return Qiandao.class;
	}

}
