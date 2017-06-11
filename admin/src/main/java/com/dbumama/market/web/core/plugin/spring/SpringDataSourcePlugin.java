package com.dbumama.market.web.core.plugin.spring;

import com.dbumama.market.service.utils.SpringContextUtil;
import com.jfinal.plugin.activerecord.IDataSourceProvider;

import javax.sql.DataSource;

/**
 * wjun_java@163.com
 * 2016年7月23日
 */
public class SpringDataSourcePlugin implements IDataSourceProvider{

	/* (non-Javadoc)
	 * @see com.jfinal.plugin.activerecord.IDataSourceProvider#getDataSource()
	 */
	@Override
	public DataSource getDataSource() {
		return (DataSource) SpringContextUtil.getApplicationContext().getBean("dataSourceProxy");
	}

}
