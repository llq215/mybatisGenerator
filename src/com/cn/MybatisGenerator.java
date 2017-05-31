package com.cn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;


/**
 * 代码生成MyBatis的实体类、dao层下实体映射(sqlmap)XML文件和Mapper类
 */
public class MybatisGenerator {
    /**
     **********************************使用前必读*******************
     ** jar包
     ** commons-lang-2.3.jar
     ** mysql-connector-java-5.1.18-bin.jar
     ** 使用前请将moduleName更改为自己数据库表名，全库生成请留空。这是Mysql版本
     **
     ***********************************************************
     */
 
    private final String type_str = "VARCHAR";
    private final String type_date = "DATE";
    private final String type_decimal = "NUMBER";
    private final String type_float = "FLOAT";
    private final String type_int = "INT";
    private final String type_text = "TEXT";
 
    private final String dbName = "qiuba";  //所在的数据库
    private final String moduleName = "t_pay_order_record"; // 表名称（所有请留空，指定表名只生成单表mapper等）
    private final String bean_package = "com.qiuba.pay.model"; //所属包路径
    private final String mapper_package = "com.qiuba.pay.dao"; //dao路径
    
    
    private static String fileName = "MybatisGenerator"; // 生成文件存储根目录文件夹名
    private final static String BASE_PATH = "D:/"+fileName+"/";
    private final static String OPEN_PATH = "D:\\"+fileName;
    private final String bean_path = BASE_PATH + "model";
    private final String mapper_path = BASE_PATH + "dao";
    //private final String xml_path = BASE_PATH + "dao/mapper";
    private final String xml_path = BASE_PATH + "mapper";
    
   
    /**
     * 数据库配置
     */
    private final String driverName = "com.mysql.jdbc.Driver";
    private final String url = "jdbc:mysql://127.0.0.1:3306/test";
    private final String user = "root";
    private final String password = "123456";
 
    private String tableName = null;
    private String beanName = null;
    private String mapperName = null;
    private Connection conn = null;
 
 
    private void init() throws ClassNotFoundException, SQLException {
        Class.forName(driverName);
        conn = DriverManager.getConnection(url, user, password);
    }
 
 
    /**
     *  获取所有的表
     *
     * @return
     * @throws SQLException 
     */
    private List<String> getTables() throws SQLException {
        List<String> tables = new ArrayList<String>();
        PreparedStatement pstate = null;
        if(StringUtils.isNotBlank(moduleName)){
        	pstate = conn.prepareStatement("select table_name from information_schema.tables where table_name ='"+moduleName+"' and table_schema = '"+dbName+"'");
        }else{
            pstate = conn.prepareStatement("select table_name from information_schema.tables");
        }
        ResultSet results = pstate.executeQuery();
        while ( results.next() ) {
            String tableName = results.getString(1);
            tables.add(tableName);
        }
        return tables;
    }
 
 
    private void processTable( String table ) {
        StringBuffer sb = new StringBuffer(table.length());
        String tableNew = table.toLowerCase();
        String[] tables = tableNew.split("_");
        String temp = null;
        if(tables.length>1){
            for ( int i = 0 ; i < tables.length ; i++ ) {
                temp = tables[i].trim();
                sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
            }
        }else{
            sb.append(tableNew.substring(0, 1).toUpperCase()).append(tableNew.substring(1));
        }
        
        beanName = sb.substring(1); //去掉以T开头表名
        mapperName = beanName + "Dao";
    }
 
 
    private String processType(String type) {
    	type = type.toUpperCase();
        if ( type.indexOf(type_str) > -1 ) {
            return "String";
        }else if ( type.indexOf(type_date) > -1 ) {
            return "java.util.Date";
        }else if ( type.indexOf(type_decimal) > -1 ) {
            return "java.math.BigDecimal";
        }else if ( type.indexOf(type_float) > -1 ) {
            return "Float";
        }else if ( type.indexOf(type_int) > -1 ) {
            return "Integer";
        }else if ( type.indexOf(type_text) > -1 ) {
            return "String";
        }
        
        
        return null;
    }
 
    private String processTypeToMysql(String type){
    	type = type.toUpperCase();
    	if(type.indexOf(type_date) > -1){
    		return "TIMESTAMP";
    	}else if(type.indexOf(type_int) > -1){
    		return "INTEGER";
    	}else if(type.indexOf(type_float) > -1){
        	return "FLOAT";
    	}else{
    		return "VARCHAR";
    	}
    }
    private String processField( String field ) {
        StringBuffer sb = new StringBuffer(field.length());
        //field = field.toLowerCase();
        String[] fields = field.split("_");
        String temp = null;
        sb.append(fields[0]);
        if(fields.length>1){
            for ( int i = 1 ; i < fields.length ; i++ ) {
                temp = fields[i].trim();
                sb.append(temp.substring(0, 1).toUpperCase()).append(temp.substring(1));
            }
        }
        return sb.toString();
    }
 
 
    /**
     *  将实体类名首字母改为小写
     *
     * @param beanName
     * @return 
     */
    private String processResultMapId( String beanName ) {
    	/*beanName = beanName.replace("t_", "");
        return beanName.substring(0, 1).toLowerCase() + beanName.substring(1);*/
    	return bean_package+"."+beanName;
    }
    /**
     * 获取mapper名字
     * @param beanName
     * @return
     */
    private String processMapperName(String beanName){
    	return beanName.substring(0, 1).toLowerCase() + beanName.substring(1);
    }
 
    /**
     *  构建类上面的注释
     *
     * @param bw
     * @param text
     * @return
     * @throws IOException 
     */
    private BufferedWriter buildClassComment( BufferedWriter bw, String text ) throws IOException {
        bw.newLine();
        bw.newLine();
        bw.write("/**");
        bw.newLine();
        bw.write(" * ");
        bw.newLine();
        bw.write(" * " + text);
        bw.newLine();
        bw.write(" * ");
        bw.newLine();
        bw.write(" **/");
        return bw;
    }
 
 
    /**
     *  构建方法上面的注释
     *
     * @param bw
     * @param text
     * @return
     * @throws IOException 
     */
    private BufferedWriter buildMethodComment( BufferedWriter bw, String text ) throws IOException {
        bw.newLine();
        bw.write("\t/**");
        bw.newLine();
        bw.write("\t * ");
        bw.newLine();
        bw.write("\t * " + text);
        bw.newLine();
        bw.write("\t * ");
        bw.newLine();
        bw.write("\t **/");
        return bw;
    }
 
 
    /**
     *  生成实体类
     *
     * @param columns
     * @param types
     * @param comments
     * @throws IOException 
     */
    private void buildEntityBean( List<String> columns, List<String> types, List<String> comments, String tableComment )
        throws IOException {
        File folder = new File(bean_path);
        if ( !folder.exists() ) {
            folder.mkdir();
        }
 
        File beanFile = new File(bean_path, beanName + ".java");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(beanFile)));
        bw.write("package " + bean_package + ";");
        bw.newLine();
        bw.write("import java.io.Serializable;");
        bw.newLine();
        //bw.write("import lombok.Data;");
        //      bw.write("import javax.persistence.Entity;");
        bw = buildClassComment(bw, tableComment);
        bw.newLine();
        bw.write("@SuppressWarnings(\"serial\")");
        bw.newLine();
        //      bw.write("@Entity");
        //bw.write("@Data");
        //bw.newLine();
        bw.write("public class " + beanName + " implements Serializable {");
        bw.newLine();
        bw.newLine();
        int size = columns.size();
        for ( int i = 0 ; i < size ; i++ ) {
            bw.write("\t/**" + comments.get(i) + "**/");
            bw.newLine();
            bw.write("\tprivate " + processType(types.get(i)) + " " + processField(columns.get(i)) + ";");
            bw.newLine();
            bw.newLine();
        }
        bw.newLine();
        // 生成get 和 set方法
        String tempField = null;
        String _tempField = null;
        String tempType = null;
        for ( int i = 0 ; i < size ; i++ ) {
            tempType = processType(types.get(i));
            _tempField = processField(columns.get(i));
            tempField = _tempField.substring(0, 1).toUpperCase() + _tempField.substring(1);
            bw.newLine();
            //          bw.write("\tpublic void set" + tempField + "(" + tempType + " _" + _tempField + "){");
            bw.write("\tpublic void set" + tempField + "(" + tempType + " " + _tempField + "){");
            bw.newLine();
            //          bw.write("\t\tthis." + _tempField + "=_" + _tempField + ";");
            bw.write("\t\tthis." + _tempField + " = " + _tempField + ";");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
            bw.newLine();
            bw.write("\tpublic " + tempType + " get" + tempField + "(){");
            bw.newLine();
            bw.write("\t\treturn this." + _tempField + ";");
            bw.newLine();
            bw.write("\t}");
            bw.newLine();
        }
        bw.newLine();
        bw.write("}");
        bw.newLine();
        bw.flush();
        bw.close();
    }
 
 
    /**
     *  构建Mapper文件
     *
     * @throws IOException 
     */
    private void buildMapper(List<String> columns, List<String> types) throws IOException {
        File folder = new File(mapper_path);
        if ( !folder.exists() ) {
            folder.mkdirs();
        }
 
        File mapperFile = new File(mapper_path, mapperName + ".java");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapperFile), "utf-8"));
        bw.write("package " + mapper_package + ";");
        bw.newLine();
        bw.newLine();
        bw.write("import " + bean_package + "." + beanName + ";");
        bw.newLine();
        bw.write("import org.apache.ibatis.annotations.Param;");
        bw.newLine();
        bw.write("import java.util.Map;");
        bw.newLine();
        bw.write("import com.qiuba.common.BootPage;"); //这是一个分页工具类
        bw.newLine();
        bw.write("import java.util.List;");
        bw = buildClassComment(bw, mapperName + "数据库操作接口类");
        bw.newLine();
        bw.newLine();
        //      bw.write("public interface " + mapperName + " extends " + mapper_extends + "<" + beanName + "> {");
        bw.write("public interface " + mapperName + "{");
        bw.newLine();
        bw.newLine();
        // 主键名称
        String pkName = processField(columns.get(0));
        String pkType = processType(types.get(0));
        // ----------定义Mapper中的方法Begin----------
        bw = buildMethodComment(bw, "分页查询");
        bw.newLine();
        bw.write("\tpublic List<" + beanName + ">  queryPage (@Param(\"params\") Map<String,Object> params,@Param(\"page\") BootPage<"+beanName+"> pageModel);");
        bw.newLine();
        bw = buildMethodComment(bw, "查询总数");
        bw.newLine();
        bw.write("\tpublic int queryPageCount (@Param(\"params\") Map<String,Object> params);");
        bw.newLine();
        bw = buildMethodComment(bw, "查询（根据主键ID查询）");
        bw.newLine();
        bw.write("\tpublic " + beanName + "  get"+beanName+"ById (@Param(\""+pkName+"\") "+pkType+" "+pkName+" );");
        bw.newLine();
        bw = buildMethodComment(bw, "删除（根据主键ID删除）");
        bw.newLine();
        bw.write("\tpublic " + "int delete"+beanName+"ById (@Param(\""+pkName+"\") "+pkType+" "+pkName+" );");
        /*bw.newLine();
        bw = buildMethodComment(bw, "添加");
        bw.newLine();
        bw.write("\tpublic " + "int insert( " + beanName + " record );");*/
        bw.newLine();
        bw = buildMethodComment(bw, "添加 （匹配有值的字段）");
        bw.newLine();
        bw.write("\tpublic " + "int insert"+beanName+"(" + beanName + " record );");
        /*bw.newLine();
        bw = buildMethodComment(bw, "修改 （匹配有值的字段）");
        bw.newLine();
        bw.write("\tpublic " + "int updateByPrimaryKeySelective( " + beanName + " record );");
        bw.newLine();*/
        bw = buildMethodComment(bw, "修改（匹配有值的字段）");
        bw.newLine();
        bw.write("\tpublic " + "int update"+beanName+" (" + beanName + " record );");
        bw.newLine();
        bw = buildMethodComment(bw, "集合（匹配有值的字段）");
        bw.newLine();
        bw.write("\tpublic List<" + beanName + ">  get"+beanName+"List (@Param(\"params\") Map<String,Object> params);");
        bw.newLine();
 
        // ----------定义Mapper中的方法End----------
        bw.newLine();
        bw.write("}");
        bw.flush();
        bw.close();
    }
 
 
    /**
     *  构建实体类映射XML文件
     *
     * @param columns
     * @param types
     * @param comments
     * @throws IOException 
     */
    private void buildMapperXml( List<String> columns, List<String> types, List<String> comments ) throws IOException {
        File folder = new File(xml_path);
        if ( !folder.exists() ) {
            folder.mkdirs();
        }
 
        File mapperXmlFile = new File(xml_path, beanName.substring(0, 1).toLowerCase() + beanName.substring(1) + "Mapper.xml");
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(mapperXmlFile)));
        bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        bw.newLine();
        bw.write("<!DOCTYPE mapper PUBLIC \"-//mybatis.org//DTD Mapper 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-mapper.dtd\">");
        bw.newLine();
        bw.write("<mapper namespace=\"" + mapper_package + "." + mapperName + "\">");
        bw.newLine();
        bw.newLine();

        buildSQL(bw, columns, types);
 
        bw.write("</mapper>");
        bw.flush();
        bw.close();
    }
 
 
    private void buildSQL( BufferedWriter bw, List<String> columns, List<String> types ) throws IOException {
        int size = columns.size();
        // 通用ResultMap
        bw.write("\t<!-- 通用ResultMap-->");
        bw.newLine();
        bw.write("\t<resultMap type=\""+processResultMapId(beanName)+"\" id=\""+processMapperName(beanName)+"Map\">");
        bw.newLine();
 
        for ( int i = 0 ; i < size ; i++ ) {
        	if("ID".equals(columns.get(i).toUpperCase())){
        		bw.write("\t\t<id property=\""+columns.get(i)+"\" column=\""+columns.get(i)+"\" jdbcType=\""+processTypeToMysql(types.get(i))+"\" />");
        		bw.newLine();
        	}else{
        		bw.write("\t\t<result property=\""+columns.get(i)+"\" column=\""+columns.get(i)+"\" jdbcType=\""+processTypeToMysql(types.get(i))+"\" />");
        		bw.newLine();
        	}
        }
 
        bw.write("\t</resultMap>");
        bw.newLine();
        bw.newLine();
        
        // 通用ResultMap
        bw.write("\t<!-- 通用条件查询-->");
        bw.newLine();
        bw.write("\t<sql id=\"Criteria_Where_Clause\">");
        bw.newLine();
        bw.write(" \t\t <where> ");
        bw.newLine();
        bw.write(" \t\t\t 1=1 ");
        bw.newLine();
        String tempCond="params.";
        for ( int i = 0 ; i < size ; i++ ) {
            bw.write("\t\t\t<if test=\"" + tempCond+columns.get(i) + " != null\">");
            bw.newLine();
            bw.write("\t\t\t\t and " + columns.get(i) + " = #{" + tempCond+columns.get(i) + ",jdbcType=VARCHAR}");
            bw.newLine();
            bw.write("\t\t\t</if>");
            bw.newLine();
        }
 
        bw.newLine();
        bw.write(" \t\t </where>");
        bw.newLine();
        bw.write("\t</sql>");
        bw.newLine();
        bw.newLine();
        
        // 通用结果列
        bw.write("\t<!-- 通用查询结果列-->");
        bw.newLine();
        bw.write("\t<sql id=\"Base_Column_List\">");
        bw.newLine();
 
        for ( int i = 0 ; i < size ; i++ ) {
            bw.write("\t"+columns.get(i));
            if ( i != size - 1 ) {
                bw.write(",");
            }
        }
 
        bw.newLine();
        bw.write("\t</sql>");
        bw.newLine();
        bw.newLine();
 
        // -----分页查询
        bw.write("\t<!-- 分页查询 -->");
        bw.newLine();
        bw.write("\t<select id=\"queryPage\" parameterType=\"Map\" resultMap=\""+processMapperName(beanName)+"Map\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t <include refid=\"Base_Column_List\" />");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t <if test=\"params != null\">");
        bw.newLine();
        bw.write("\t\t\t <include refid=\"Criteria_Where_Clause\" />");
        bw.newLine();
        bw.write("\t\t </if>");
        bw.newLine();
        bw.write("\t\t limit #{page.offset} , #{page.limit}");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();
        
        // -----分页查询结果数
        bw.write("\t<!-- 分页总数查询 -->");
        bw.newLine();
        bw.write("\t<select id=\"queryPageCount\" resultType=\"int\" parameterType=\"Map\" >");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t count(0)");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t <if test=\"params != null\">");
        bw.newLine();
        bw.write("\t\t\t <include refid=\"Criteria_Where_Clause\" />");
        bw.newLine();
        bw.write("\t\t </if>");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();
        
        // ----- 集合（匹配有值的字段）
        bw.write("\t<!-- 集合（匹配有值的字段）-->");
        bw.newLine();
        bw.write("\t<select id=\"get"+beanName+"List\" parameterType=\"Map\" resultMap=\""+processMapperName(beanName)+"Map\" >");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t <include refid=\"Base_Column_List\" />");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t <if test=\"params != null\">");
        bw.newLine();
        bw.write("\t\t\t <include refid=\"Criteria_Where_Clause\" />");
        bw.newLine();
        bw.write("\t\t </if>");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();
        
        // 查询（根据主键ID查询）
        bw.write("\t<!-- 查询（根据主键ID查询） -->");
        bw.newLine();
        bw.write("\t<select id=\"get"+beanName+"ById\" parameterType=\"Integer\" resultMap=\"" + processMapperName(beanName)+"Map\">");
        bw.newLine();
        bw.write("\t\t SELECT");
        bw.newLine();
        bw.write("\t\t <include refid=\"Base_Column_List\" />");
        bw.newLine();
        bw.write("\t\t FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + "}");
        bw.newLine();
        bw.write("\t</select>");
        bw.newLine();
        bw.newLine();
        // 查询完
 
 
        // 删除（根据主键ID删除）
        bw.write("\t<!--删除：根据主键ID删除-->");
        bw.newLine();
        bw.write("\t<delete id=\"delete"+beanName+"ById\" parameterType=\"Integer\">");
        bw.newLine();
        bw.write("\t\t DELETE FROM " + tableName);
        bw.newLine();
        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + "}");
        bw.newLine();
        bw.write("\t</delete>");
        bw.newLine();
        bw.newLine();
        // 删除完
 
 
        // 添加insert方法
        /*bw.write("\t<!-- 添加 -->");
        bw.newLine();
        bw.write("\t<insert id=\"insert\" parameterType=\"" + processResultMapId(beanName) + "\">");
        bw.newLine();
        bw.write("\t\t INSERT INTO " + tableName);
        bw.newLine();
        bw.write(" \t\t(");
        for ( int i = 0 ; i < size ; i++ ) {
            bw.write(columns.get(i));
            if ( i != size - 1 ) {
                bw.write(",");
            }
        }
        bw.write(") ");
        bw.newLine();
        bw.write("\t\t VALUES ");
        bw.newLine();
        bw.write(" \t\t(");
        for ( int i = 0 ; i < size ; i++ ) {
            bw.write("#{" + processField(columns.get(i)) + "}");
            if ( i != size - 1 ) {
                bw.write(",");
            }
        }
        bw.write(") ");
        bw.newLine();
        bw.write("\t</insert>");
        bw.newLine();
        bw.newLine();*/
        // 添加insert完
 
 
        //---------------  insert方法（匹配有值的字段）
        bw.write("\t<!-- 添加 （匹配有值的字段）-->");
        bw.newLine();
        bw.write("\t<insert id=\"insert"+beanName+"\" parameterType=\"" + processResultMapId(beanName) + "\" useGeneratedKeys=\"true\" keyProperty=\"id\">");
        bw.newLine();
        bw.write("\t\t INSERT INTO " + tableName);
        bw.newLine();
        bw.write("\t\t <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\" >");
        bw.newLine();
 
        String tempField = null;
        for ( int i = 0 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t<if test=\"" + tempField + " != null\">");
            bw.newLine();
            bw.write("\t\t\t\t " + columns.get(i) + ",");
            bw.newLine();
            bw.write("\t\t\t</if>");
            bw.newLine();
        }
 
        bw.newLine();
        bw.write("\t\t </trim>");
        bw.newLine();
 
        bw.write("\t\t <trim prefix=\"values (\" suffix=\")\" suffixOverrides=\",\" >");
        bw.newLine();
 
        tempField = null;
        for ( int i = 0 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t<if test=\"" + tempField + "!=null\">");
            bw.newLine();
            bw.write("\t\t\t\t #{" + tempField + "},");
            bw.newLine();
            bw.write("\t\t\t</if>");
            bw.newLine();
        }
 
        bw.write("\t\t </trim>");
        bw.newLine();
        bw.write("\t</insert>");
        bw.newLine();
        bw.newLine();
        //---------------  完毕
 
 
        // 修改update方法
        bw.write("\t<!-- 修 改-->");
        bw.newLine();
        bw.write("\t<update id=\"update"+beanName+"\" parameterType=\"" + processResultMapId(beanName) + "\">");
        bw.newLine();
        bw.write("\t\t UPDATE " + tableName);
        bw.newLine();
        bw.write(" \t\t <set> ");
        bw.newLine();
 
        tempField = null;
        for ( int i = 1 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t<if test=\"" + tempField + " != null\">");
            bw.newLine();
            bw.write("\t\t\t\t " + columns.get(i) + " = #{" + tempField + "},");
            bw.newLine();
            bw.write("\t\t\t</if>");
            bw.newLine();
        }
 
        bw.newLine();
        bw.write(" \t\t </set>");
        bw.newLine();
        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + "}");
        bw.newLine();
        bw.write("\t</update>");
        bw.newLine();
        bw.newLine();
        // update方法完毕
 
        // ----- 修改（匹配有值的字段）
        /*bw.write("\t<!-- 修 改（匹配有值的字段）-->");
        bw.newLine();
        bw.write("\t<update id=\"updateByPrimaryKey\" parameterType=\"" + processResultMapId(beanName) + "\">");
        bw.newLine();
        bw.write("\t\t UPDATE " + tableName);
        bw.newLine();
        bw.write("\t\t SET ");
 
        bw.newLine();
        tempField = null;
        for ( int i = 1 ; i < size ; i++ ) {
            tempField = processField(columns.get(i));
            bw.write("\t\t\t " + columns.get(i) + " = #{" + tempField + "}");
            if ( i != size - 1 ) {
                bw.write(",");
            }
            bw.newLine();
        }
 
        bw.write("\t\t WHERE " + columns.get(0) + " = #{" + processField(columns.get(0)) + "}");
        bw.newLine();
        bw.write("\t</update>");
        bw.newLine();
        bw.newLine();*/
        //---------------  完毕
        
        bw.newLine();
    }
 
 
    public void generate() throws ClassNotFoundException, SQLException, IOException {
        init();
        //String prefix = "select column_name,data_type from user_tab_columns where  table_name=";
        String prefix = "select column_name,data_type,column_comment from information_schema.columns where  table_name=";
        List<String> columns = null;
        List<String> types = null;
        List<String> comments = null;
        PreparedStatement pstate = null;
        List<String> tables = getTables();
        for ( String table : tables ) {
            columns = new ArrayList<String>();
            types = new ArrayList<String>();
            comments = new ArrayList<String>();
            pstate = conn.prepareStatement(prefix +"'"+ table +"' and table_schema = '"+dbName+"'");
            ResultSet results = pstate.executeQuery();
            while ( results.next() ) {
                columns.add(results.getString("column_name"));
                types.add(results.getString("data_type"));
                comments.add(results.getString("column_comment"));
            }
            tableName = table;
            processTable(table);
            buildEntityBean(columns, types, comments, "数据库表名称:"+tableName);
            buildMapper(columns, types);
            buildMapperXml(columns, types, comments);
        }
        conn.close();
    }
    /**
     * 删除单个文件
     * @param   sPath    被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String sPath) {
        boolean flag = false;
        File file = new File(sPath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }
    /**
     * 删除目录（文件夹）以及目录下的文件
     * @param   sPath 被删除目录的文件路径
     * @return  目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sPath) {
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sPath.endsWith(File.separator)) {
            sPath = sPath + File.separator;
        }
        File dirFile = new File(sPath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }
 
    /**
     * 生成入口
     * @param args
     */
    public static void main( String[] args ) {
        try {
            // 自动打开生成文件的目录
            File folder = new File(BASE_PATH);
            // 判断是否为文件  
            if (folder.isFile()) {  // 为文件时调用删除文件方法  
                 deleteFile(BASE_PATH);  
            } else {  // 为目录时调用删除目录方法  
                 deleteDirectory(BASE_PATH);  
            }  
            folder.mkdir();
            new MybatisGenerator().generate();
            Runtime.getRuntime().exec("cmd /c start explorer "+OPEN_PATH);
        } catch ( ClassNotFoundException e ) {
            e.printStackTrace();
        } catch ( SQLException e ) {
            e.printStackTrace();
        } catch ( IOException e ) {
            e.printStackTrace();
        }
    }
    
}
