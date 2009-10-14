package net.paoding.rose.jade.jadeinterface.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.paoding.rose.jade.jadeinterface.annotation.SQL;
import net.paoding.rose.jade.jadeinterface.annotation.SQLParam;
import net.paoding.rose.jade.jadeinterface.annotation.SQLReturn;
import net.paoding.rose.jade.jadeinterface.annotation.SQLReturnType;
import net.paoding.rose.jade.jadeinterface.provider.DataAccess;

import org.springframework.util.NumberUtils;

/**
 * 
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 */
public class UpdateOperation implements JdbcOperation {

    private RowMapperFactory rowMapperFactory;

    public void setRowMapperFactory(RowMapperFactory rowMapperFactory) {
        this.rowMapperFactory = rowMapperFactory;
    }

    public RowMapperFactory getRowMapperFactory() {
        return rowMapperFactory;
    }

    @Override
    public Object execute(DataAccess dataAccess, Class<?> daoClass, Method method, Object[] args) {

        SQL sqlCommand = method.getAnnotation(SQL.class);

        // 将参数放入 map 中
        Map<String, Object> parameters = new HashMap<String, Object>();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        for (int i = 0; i < parameterAnnotations.length; i++) {
            Annotation[] annotations = parameterAnnotations[i];
            for (Annotation annotation : annotations) {
                if (annotation instanceof SQLParam) {
                    parameters.put(((SQLParam) annotation).value(), args[i]);
                    continue;
                }
            }
        }

        // 检查返回值类型
        SQLReturn sqlReturn = method.getAnnotation(SQLReturn.class);
        if (SQLReturnType.ID == sqlReturn.value()) {

            // 执行 INSERT 查询
            return dataAccess.insertReturnId(sqlCommand.value(), parameters);

        } else if (SQLReturnType.UPDATE_COUNT == sqlReturn.value()) {

            // 执行 UPDATE / DELETE 查询
            int updated = dataAccess.update(sqlCommand.value(), parameters);

            // 将结果转成方法的返回类型
            Class<?> returnClassType = method.getReturnType();
            if (returnClassType == int.class || returnClassType == Integer.class
                    || returnClassType == long.class || returnClassType == Long.class) {
                return updated;
            }

            if (Number.class.isAssignableFrom(returnClassType)) {
                return NumberUtils.parseNumber(String.valueOf(updated), returnClassType);
            }

            if (returnClassType == boolean.class || returnClassType == Boolean.class) {
                return updated > 0;
            }
        }

        return null;
    }
}