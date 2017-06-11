package com.junlin.manager.utils;


import com.jayway.jsonpath.Criteria;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

import java.util.List;
import java.util.logging.Logger;

/**
 * SQL语句生成封装，参考Hibernate的QBC
 */
public class SqlHelper extends Db {

    //logger
    private static final Logger logger = Logger.getLogger(Criteria.class.getName());
    //key
    private static final String EQUAL = " = ";
    private static final String NO_EQUAL = " <> ";
    private static final String LT = " < ";
    private static final String LE = " <= ";
    private static final String GE = " >= ";
    private static final String GT = " > ";
    private static final String BETWEEN = " between ";
    private static final String NO_BETWEEN = " not between ";
    private static final String IS_NULL = " is null";
    private static final String NOT_NULL = " is not null";
    private static final String LIKE = " LIKE ";
    private static final String PLACEHOLDER = "?";
    private static final String PERCENT_START = "'%";
    private static final String PERCENT_MID = "%";
    private static final String PERCENT_END = "%' ";
    private static final String WHERE_STR = " where 1=1 ";
    //语句
    private StringBuffer sqlSelect = null;
    private StringBuffer sqlExceptSelect = null;
    //对象
    private static SqlHelper criteria;

    @SuppressWarnings("rawtypes")
    private List resultList = null;

    /**
     * 查询
     *
     * @param <T>
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> find() {
        if (sqlNotNull()) {
            resultList = Db.find(sqlSelect.toString() + sqlExceptSelect.toString());
        }
        return resultList;
    }

    /**
     * 查询第一个
     */
    @SuppressWarnings("unchecked")
    public Record findFirst() {
        if (sqlNotNull()) {
            return Db.findFirst(sqlSelect.toString() + sqlExceptSelect.toString());
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> paginate(int pageNumber, int pageSize) {
        if (sqlNotNull()) {
            logger.info("\nselect子句：" + sqlSelect.toString() + "\nfrom子句:" + sqlExceptSelect.toString());
            Page<Record> pages = Db.paginate(pageNumber, pageSize, sqlSelect.toString(), sqlExceptSelect.toString());
            resultList = pages.getList();
        }
        return resultList;
    }

    /**
     * 查询数据
     *
     * @return long
     */
    public Long queryCount(String param) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT count(DISTINCT " + param + ") ").append(sqlExceptSelect.toString());
        return Db.queryLong(sql.toString());
    }

    /**
     * 通过类获取表名
     *
     * @param clazz
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static SqlHelper fromEntityClass(Class clazz) {
        criteria = new SqlHelper(clazz.getSimpleName());
        return criteria;
    }

    /**
     * 通过类与参数获取sql
     *
     * @param clazz
     * @param obtainParams
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static SqlHelper fromEntityClass(Class clazz, String... obtainParams) {
        criteria = new SqlHelper(clazz.getName(), obtainParams);
        return criteria;
    }

    /**
     * 通过类与参数获取sql
     *
     * @param tableName
     * @param obtainParams
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static SqlHelper fromTableName(String tableName, String... obtainParams) {
        criteria = new SqlHelper(tableName, obtainParams);
        return criteria;
    }

    /**
     * 通过类与参数获取sql
     *
     * @param entityNames
     * @param obtainParams
     * @return
     */
    @SuppressWarnings("rawtypes")
    public static SqlHelper fromTableName(String[] entityNames, String... obtainParams) {
        criteria = new SqlHelper(entityNames, obtainParams);
        return criteria;
    }


    /**
     * 生成SQL
     *
     * @param
     * @return
     */
    public String buildSql() {
        if (sqlSelect == null || sqlExceptSelect == null) {
            throw new RuntimeException("解析SQL语句出错:无效的SQL语句");
        } else {
            return sqlSelect.toString().toLowerCase() + sqlExceptSelect.toString().toLowerCase();
        }
    }

    /**
     * 生成SQL
     *
     * @return
     */
    public String buildSelectSQL() {
        if (sqlSelect == null || sqlExceptSelect == null) {
            throw new RuntimeException("解析SQL语句出错:无效的SQL语句");
        } else {
            return sqlSelect.toString().toLowerCase();
        }
    }

    /**
     * 生成SQL
     *
     * @return
     */
    public String buildExceptSelectSQL() {
        if (sqlSelect == null || sqlExceptSelect == null) {
            throw new RuntimeException("解析SQL语句出错:无效的SQL语句");
        } else {
            return sqlExceptSelect.toString().toLowerCase();
        }
    }

    ///////////////////////////////where子句////////////////////////////////////////////////////////

    /**
     * 字段值为空
     *
     * @param param 字段名
     */
    public SqlHelper isNull(String param) {
        sqlExceptSelect.append(" and ").append(param).append(IS_NULL);
        return this;
    }

    /**
     * 字段值非空
     *
     * @param param 字段名
     */
    public SqlHelper notNull(String param) {
        sqlExceptSelect.append(" and ").append(param).append(NOT_NULL);
        return this;
    }

    /**
     * 字段值不等于给定值
     *
     * @param param 字段名
     * @param value 给定值
     */
    public SqlHelper ne(String param, Object value) {
        sqlExceptSelect.append(" and ").append(param).append(NO_EQUAL)
                .append(value);
        return this;
    }

    /**
     * 字段值等于给定值
     *
     * @param param 字段名
     * @param value 给定值
     */
    public SqlHelper eq(String param, Object value) {
        sqlExceptSelect.append(" and ").append(param).append(EQUAL).append(value);
        return this;
    }

    /**
     * 字段值小于给定值
     *
     * @param param 字段名
     * @param value 给定值
     */
    public SqlHelper lt(String param, Object value) {
        sqlExceptSelect.append(" and ").append(param).append(LT).append(value);
        return this;
    }

    /**
     * 字段值小于或者等于给定值
     *
     * @param param 字段名
     * @param value 给定值
     */
    public SqlHelper le(String param, Object value) {
        sqlExceptSelect.append(" and ").append(param).append(LE).append(value);
        return this;
    }

    /**
     * 字段值大于给定值
     *
     * @param param 字段名
     * @param value 给定值
     */
    public SqlHelper gt(String param, Object value) {
        sqlExceptSelect.append(" and ").append(param).append(GT).append(value);
        return this;
    }

    /**
     * 字段值大于或者等于给定值
     *
     * @param param 字段名
     * @param value 给定值
     */
    public SqlHelper ge(String param, Object value) {
        sqlExceptSelect.append(" and ").append(param).append(GE).append(value);
        return this;
    }

    /**
     * 字段值在某范围内
     *
     * @param paramName 字段名
     * @param firstVal  范围的起始
     * @param secondVal 范围的结束
     */
    public SqlHelper between(String paramName, Object firstVal, Object secondVal) {
        sqlExceptSelect.append(" and ").append(paramName).append(BETWEEN)
                .append(firstVal).append(" and ").append(secondVal);
        return this;
    }

    /**
     * 字段值不在某范围内
     *
     * @param paramName 字段名
     * @param firstVal  范围的起始
     * @param secondVal 范围的结束
     */
    public SqlHelper nb(String paramName, Object firstVal, Object secondVal) {
        sqlExceptSelect.append(" and ").append(paramName).append(NO_BETWEEN)
                .append(firstVal).append(" and ").append(secondVal);
        return this;
    }

    /**
     * 模糊匹配
     *
     * @param paramName 字段名
     * @param keyWord
     */
    public SqlHelper likeWith(String paramName, String keyWord) {
        sqlExceptSelect.append(" and ").append(paramName).append(LIKE).append(PERCENT_START).append(keyWord).append(PERCENT_END);
        return this;
    }

    /**
     * 模糊匹配
     *
     * @param paramName 字段名
     * @param keyWord
     */
    public SqlHelper likeStart(String paramName, String keyWord) {
        sqlExceptSelect.append(" and ").append(paramName).append(LIKE).append("'" + keyWord).append(PERCENT_MID + "'");
        return this;
    }

    /**
     * 模糊匹配
     *
     * @param paramName 字段名
     * @param keyWord
     */
    public SqlHelper likeOr(String paramName, String keyWord) {
        sqlExceptSelect.append(" or ").append(paramName).append(LIKE).append(PERCENT_START).append(keyWord).append(PERCENT_END);
        return this;
    }

    /**
     * 根据某个值降序排序
     *
     * @param paramName
     * @return
     */
    public SqlHelper orderByDesc(String paramName) {
        sqlExceptSelect.append(" order by ").append(paramName).append(" desc");
        return this;
    }

    /**
     * 根据某个值降序排序
     *
     * @param paramName
     * @return
     */
    public SqlHelper gruopBy(String paramName) {
        sqlExceptSelect.append(" GROUP BY ").append(paramName);
        return this;
    }

    /**
     * 左联
     *
     * @param tableName
     * @param columnA
     * @param columnB
     * @return
     */
    public SqlHelper leftJoin(String tableName, String columnA, String columnB) {
        StringBuilder joinBuilder = new StringBuilder("LEFT JOIN ").append(tableName).append(" on ").append(columnA).append(" = ").append
                (columnB).append(" where");
        String[] sqlExceptSelects = sqlExceptSelect.toString().split("where");
        sqlExceptSelect = null;
        sqlExceptSelect = new StringBuffer(sqlExceptSelects[0]).append(joinBuilder).append(sqlExceptSelects[1]);
        return this;
    }

    /**
     * 右联
     *
     * @param tableName
     * @param columnA
     * @param columnB
     * @return
     */
    public SqlHelper rightJoin(String tableName, String columnA, String columnB) {
        StringBuilder joinBuilder = new StringBuilder("RIGHT JOIN ").append(tableName).append(" on ").append(columnA).append(" = ").append
                (columnB).append(" where");
        String[] sqlExceptSelects = sqlExceptSelect.toString().split("where");
        sqlExceptSelect = null;
        sqlExceptSelect = new StringBuffer(sqlExceptSelects[0]).append(joinBuilder).append(sqlExceptSelects[1]);
        return this;
    }

    /**
     * 内联
     *
     * @param tableName
     * @param columnA
     * @param columnB
     * @return
     */
    public SqlHelper innerJoin(String tableName, String columnA, String columnB) {
        StringBuilder joinBuilder = new StringBuilder("INNER JOIN ").append(tableName).append(" on ").append(columnA).append(" = ").append
                (columnB).append(" where");
        String[] sqlExceptSelects = sqlExceptSelect.toString().split("where");
        sqlExceptSelect = null;
        sqlExceptSelect = new StringBuffer(sqlExceptSelects[0]).append(joinBuilder).append(sqlExceptSelects[1]);
        return this;
    }


    ///////////////////////////////////////////////////////////////


    /**
     * 获取表中所有字段
     *
     * @param entityName 实体名称
     */
    private SqlHelper(String entityName) {
        sqlSelect = new StringBuffer("select * ");
        sqlExceptSelect = new StringBuffer(" from ").append(entityName).append(WHERE_STR);
    }

    /**
     * 获取表中局部字段
     *
     * @param entityName   实体名称
     * @param obtainParams 提供需要获取值的字段名
     */
    private SqlHelper(String entityName, String... obtainParams) {
        sqlSelect = new StringBuffer("select ");
        if (obtainParams != null) {
            for (String str : obtainParams) {
                sqlSelect.append(str).append(",");
            }
            int lastIndex = sqlSelect.lastIndexOf(",");
            sqlSelect.delete(lastIndex, lastIndex + 1);
        }
        sqlExceptSelect = new StringBuffer(" from ").append(entityName).append(WHERE_STR);
    }


    /**
     * 获取表中局部字段
     *
     * @param entityNames  实体名称
     * @param obtainParams 提供需要获取值的字段名
     */
    private SqlHelper(String[] entityNames, String... obtainParams) {
        sqlSelect = new StringBuffer("select ");
        if (obtainParams != null) {
            for (String str : obtainParams) {
                sqlSelect.append(str).append(",");
            }
            int lastIndex = sqlSelect.lastIndexOf(",");
            sqlSelect.delete(lastIndex, lastIndex + 1);
        }

        sqlExceptSelect = new StringBuffer(" from ");
        if (entityNames != null) {
            for (String str : entityNames) {
                sqlExceptSelect.append(str).append(",");
            }
            //删除最后一个逗号
            int lastIndex = sqlExceptSelect.lastIndexOf(",");
            sqlExceptSelect.delete(lastIndex, lastIndex + 1);
        }
        sqlExceptSelect.append(WHERE_STR);
    }


    /**
     * 检测sql语句
     *
     * @return bool
     */
    private boolean sqlNotNull() {
        if (sqlSelect != null) {
            return true;
        } else {
            throw new RuntimeException("解析SQL语句出错");
        }
    }
}