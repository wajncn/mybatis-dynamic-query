package com.github.wz2cool.dynamic.mybatis.mapper.provider.factory;

import com.github.wz2cool.dynamic.helper.CommonsHelper;

import java.util.Arrays;
import java.util.stream.Collectors;

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


    /**
     * @param insertSelective insertSelective
     * @return sql
     */
    public String getInsertSql(boolean insertSelective) {
        if (insertSelective) {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append("<script>");
            sqlBuilder.append("insert into ");
            sqlBuilder.append(providerTable.getTableName());
            sqlBuilder.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
            for (ProviderColumn column : providerTable.getColumns()) {
                sqlBuilder.append(CommonsHelper.format("<if test=\"%s != null\">%s,</if>",
                        column.getJavaColumn(), column.getDbColumn()));
            }
            sqlBuilder.append("</trim>");

            sqlBuilder.append("<trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\">");
            for (ProviderColumn column : providerTable.getColumns()) {
                sqlBuilder.append(CommonsHelper.format("<if test=\"%s != null\">#{%s},</if>",
                        column.getJavaColumn(), column.getJavaColumn()));
            }
            sqlBuilder.append("</trim>");
            sqlBuilder.append("</script>");
            return sqlBuilder.toString();
        }
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("<script>");
        sqlBuilder.append("insert into ");
        sqlBuilder.append(providerTable.getTableName());
        sqlBuilder.append("(");
        sqlBuilder.append(Arrays.stream(providerTable.getColumns())
                .map(ProviderColumn::getDbColumn).collect(Collectors.joining(",")));
        sqlBuilder.append(") values (");
        sqlBuilder.append(Arrays.stream(providerTable.getColumns())
                .map(ProviderColumn::getJavaColumn)
                .map(a -> CommonsHelper.format("#{%s}", a))
                .collect(Collectors.joining(",")));
        sqlBuilder.append(")");
        sqlBuilder.append("</script>");
        return sqlBuilder.toString();
    }


}
