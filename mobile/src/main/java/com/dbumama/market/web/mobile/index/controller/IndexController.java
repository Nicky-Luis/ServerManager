/**
 * 文件名:IndexController.java
 * 版本信息:1.0
 * 日期:2015-5-17
 * Copyright 广州点步信息科技
 * 版权所有
 */
package com.dbumama.market.web.mobile.index.controller;


import com.alibaba.druid.util.StringUtils;
import com.dbumama.market.model.Product;
import com.dbumama.market.model.ProductCategory;
import com.dbumama.market.model.Shop;
import com.dbumama.market.service.api.product.ProductMobileParamDto;
import com.dbumama.market.service.api.product.ProductMobileResultDto;
import com.dbumama.market.service.api.product.ProductService;
import com.dbumama.market.service.api.user.PhoneCodeService;
import com.dbumama.market.web.core.annotation.RouteBind;
import com.dbumama.market.web.core.controller.BaseMobileController;
import com.dbumama.market.web.core.plugin.spring.Inject;
import com.dbumama.market.web.core.utils.IpKit;
import com.jfinal.aop.Clear;

import java.util.ArrayList;
import java.util.List;

import cn.dreampie.captcha.CaptchaRender;

/**
 * @author: wjun.java@gmail.com
 * @date:2015-5-17
 */
@RouteBind(path="/")
public class IndexController extends BaseMobileController {

	@Inject.BY_NAME
	private PhoneCodeService phoneCodeService;
	
	@Inject.BY_NAME
	private ProductService productService;
	
	public void index(){
		//查询卖家创建的活动列表 在有效时间范围内可见的 并且是没有删除的活动 并且是进行中的活动
		Shop shop = Shop.dao.findFirst("select * from " + Shop.table + " where seller_id= ?", getSellerId());
		setAttr("shop", shop);
		
		ProductMobileParamDto mobileParamDto = new ProductMobileParamDto(getSellerId(), getPageNo());
		//查询商品列表
		List<ProductMobileResultDto> indexProducts = productService.getIndexProduct(mobileParamDto);
		setAttr("indexProducts", indexProducts);
		List<ProductMobileResultDto> hotProducts = productService.getHotProduct(mobileParamDto);
		setAttr("hotProducts", hotProducts);
		List<ProductMobileResultDto> newProducts = productService.getNewProduct(mobileParamDto);
		setAttr("newProducts", newProducts);
		List<ProductMobileResultDto> commondProducts = productService.getRecommendProduct(mobileParamDto);
		setAttr("commondProducts", commondProducts);
		List<ProductCategory> productCategorys = ProductCategory.dao.findRoots(null);
		List<ProductCategory> validCategorys = new ArrayList<ProductCategory>();
		for(ProductCategory pcate : productCategorys){
			List<Product> products = Product.dao.find("select * from " + Product.table + " where product_category_id=? and active=1 and is_marketable=1 ", pcate.getId());
			if(products != null && products.size()>0){
				validCategorys.add(pcate);
			}
		}
		setAttr("productCategory", validCategorys);
	}
	
	public void forbid(){}
	public void error(){}
	public void auth(){setAttr("rUrl", getPara("rUrl"));}
	
	public void sendCode(){
		final String phone = getPara("phone");
		if(StringUtils.isEmpty(phone)){
			rendFailedJson("手机号码为空");
			return;
		}
		rendSuccessJson(phoneCodeService.getCodeByAgent(phone, IpKit.getRealIpV2(getRequest())));
	}
	
	@Clear
    public void captcha() {
        int width = 0, height = 0, minnum = 0, maxnum = 0, fontsize = 0;
        CaptchaRender captcha = new CaptchaRender();
        if (isParaExists("width")) {
            width = getParaToInt("width");
        }
        if (isParaExists("height")) {
            height = getParaToInt("height");
        }
        if (width > 0 && height > 0)
            captcha.setImgSize(width, height);
        if (isParaExists("minnum")) {
            minnum = getParaToInt("minnum");
        }
        if (isParaExists("maxnum")) {
            maxnum = getParaToInt("maxnum");
        }
        if (minnum > 0 && maxnum > 0)
            captcha.setFontNum(minnum, maxnum);
        if (isParaExists("fontsize")) {
            fontsize = getParaToInt("fontsize");
        }
        if (fontsize > 0)
            captcha.setFontSize(fontsize, fontsize);
        // 干扰线数量 默认0
        captcha.setLineNum(1);
        // 噪点数量 默认50
        captcha.setArtifactNum(10);
        // 使用字符 去掉0和o 避免难以确认
        captcha.setCode("123456789");
        //验证码在session里的名字 默认 captcha,创建时间为：名字_time
        // captcha.setCaptchaName("captcha");
        //验证码颜色 默认黑色，
        // captcha.setDrawColor(new Color(255,0,0));
        //背景干扰物颜色  默认灰
        // captcha.setDrawBgColor(new Color(0,0,0));
        //背景色+透明度 前三位数字是rgb色，第四个数字是透明度  默认透明
        // captcha.setBgColor(new Color(225, 225, 0, 100));
        //滤镜特效 默认随机特效 //曲面Curves //大理石纹Marble //弯折Double //颤动Wobble //扩散Diffuse
        captcha.setFilter(CaptchaRender.FilterFactory.Curves);
        // 随机色 默认黑验证码 灰背景元素
        captcha.setRandomColor(true);
        render(captcha);
    }
}
