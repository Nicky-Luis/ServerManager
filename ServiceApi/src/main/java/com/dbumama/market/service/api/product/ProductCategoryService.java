package com.dbumama.market.service.api.product;

import com.dbumama.market.model.ProductCategory;

import java.util.List;

/**
 * 
 * @author drs
 *  商品分类
 */
public interface ProductCategoryService {
	/**
	 * 查找顶级商品分类
	 * 
	 * @return 顶级商品分类
	 */
	public List<ProductCategory> findRoots();
	
	
	public ProductCategory find(Long productCategoryId);
}
