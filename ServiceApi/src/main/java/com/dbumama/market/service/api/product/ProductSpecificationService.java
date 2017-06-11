package com.dbumama.market.service.api.product;

import com.dbumama.market.model.ProductSpecification;
import com.dbumama.market.model.ProductSpecificationValue;

import java.util.List;

public interface ProductSpecificationService {
	/*public boolean save(Product product);
	public boolean update(Product product);
	public boolean delete(Long products);*/
	
	public List<ProductSpecification> getSpecificationsByProduct(Long productId) throws ProductException;
	public List<ProductSpecificationValue> getSpecificationVaulesByProduct(Long productId) throws ProductException;
}
