package com.dbumama.market.service.api.specification;

import com.dbumama.market.model.SellerUser;
import com.dbumama.market.model.Specification;

import java.util.List;

public interface SpecificationService {
      public List<SpecificationResultDto> findAll(SpecificationParamDto specificationParamDto);
      
      public Specification find(Long specificationIds);
      
      public Specification doSave(Specification specification,String items, SellerUser sellerUser);
      
      public Specification doUpdate(Specification specification,String items, SellerUser sellerUser);

}
