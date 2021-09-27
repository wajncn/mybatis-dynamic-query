package com.github.wz2cool.dynamic.mybatis.mapper.provider.factory;

import com.github.wz2cool.dynamic.helper.CommonsHelper;

/**
 * 通过本类可以获取动态sql
 *
 * @author wangjin
 */
public final class DynamicCreateSqlFactory {

    private final ProviderTable providerTable;

    private DynamicCreateSqlFactory(ProviderTable providerTable) {
        this.providerTable = providerTable;
    }

    public static DynamicCreateSqlFactory getSqlFactory(ProviderTable providerTable) {
        return new DynamicCreateSqlFactory(providerTable);
    }


    /**
     * @param deleteByPrimaryKey 如果是deleteByPrimaryKey, 则说明只通过主键删除
     * @return sql
     */
    public String getDeleteSql(boolean deleteByPrimaryKey) {
        if (deleteByPrimaryKey) {
            if (providerTable.getPrimaryKey() == null) {
                throw new IllegalArgumentException(CommonsHelper.format("该类[%s]没有发现主键", providerTable.getTableName()));
            }
            // 简单sql
            return CommonsHelper.format("delete from %s where %s = #{%s}",
                    providerTable.getTableName(), providerTable.getPrimaryKey().getDbColumn(), providerTable.getPrimaryKey().getJavaColumn());
        }

        //复杂sql <script>
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("<script>");
        sqlBuilder.append(CommonsHelper.format("delete from %s ", providerTable.getTableName()));
        sqlBuilder.append("<where>");
        for (ProviderColumn column : providerTable.getColumns()) {
            sqlBuilder.append(CommonsHelper.format("<if test=\"%s != null\"> and %s = #{%s}</if>",
                    column.getJavaColumn(), column.getDbColumn(), column.getJavaColumn()));
        }
        sqlBuilder.append("</where>");
        sqlBuilder.append("</script>");
        return sqlBuilder.toString();
    }


}
