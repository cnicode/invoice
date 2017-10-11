package com.hl.dao.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import com.alibaba.fastjson.JSON;
import com.hl.dao.InvoiceDao;
import com.hl.domain.Model;
import com.hl.util.Const;
import com.mysql.jdbc.TimeUtil;
import com.sun.istack.FinalArrayList;

public class InvoiceDaoImpl extends JdbcDaoSupport implements InvoiceDao{

	@Override
	public Integer addRecognizeAction(final Integer user_id) {
		//生成一条行为，插入action表，并获取返回的action_id(主键) 
		final String sql = "insert into invoice.action values(null,?,1,0,null,null,?,null,null)";
		final String action_start_time = com.hl.util.TimeUtil.getCurrentTime();
		KeyHolder keyHolder = new GeneratedKeyHolder();
		//返回主键
		getJdbcTemplate().update(new PreparedStatementCreator() {
			@Override
			public java.sql.PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				java.sql.PreparedStatement psm =  connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				psm.setInt(1,user_id);
				psm.setString(2, action_start_time);
				return psm;
			}
		},keyHolder);
		return keyHolder.getKey().intValue();
	}

	@Override
	public Integer addNewModelAction(final Integer user_id) {
		//增加一条增加发票模板的操作,返回主键
		final String sql = "insert into invoice.action values(null,?,2,0,null,null,?,null,null)";
		final String action_start_time = com.hl.util.TimeUtil.getCurrentTime();
		KeyHolder keyHolder = new GeneratedKeyHolder();
		//返回主键
		getJdbcTemplate().update(new PreparedStatementCreator() {
			@Override
			public java.sql.PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				java.sql.PreparedStatement psm =  connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				psm.setInt(1,user_id);
				psm.setString(2, action_start_time);
				return psm;
			}
		},keyHolder);
		return keyHolder.getKey().intValue();
	}
	
	@Override
	public Integer addUpdateModelAction(final Integer user_id, final Integer model_id) {
		//增加修改发票模板的行为，返回主键
		final String sql = "insert into invoice.action values(null,?,4,0,null,?,?,null,null)";
		final String action_start_time = com.hl.util.TimeUtil.getCurrentTime();
		KeyHolder keyHolder = new GeneratedKeyHolder();
		//返回主键
		getJdbcTemplate().update(new PreparedStatementCreator() {
			@Override
			public java.sql.PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				java.sql.PreparedStatement psm =  connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				psm.setInt(1,user_id);
				psm.setInt(2, model_id);
				psm.setString(3, action_start_time);
				return psm;
			}
		},keyHolder);
		return keyHolder.getKey().intValue();
	}
	
	@Override
	public int addRecognizeInvoice(final Integer action_id, Map<String, Object> invoice_data,
			final Integer model_id,final String url) {
		//添加一条发票信息
		final String invoice_type = (String) invoice_data.get("发票类型");
		final String invoice_money = (String) invoice_data.get("金额");
		final String invoice_customer = (String) invoice_data.get("客户名称");
		final String invoice_code = (String) invoice_data.get("发票号码");
		final String invoice_date = (String) invoice_data.get("日期");
		final String invoice_time = (String) invoice_data.get("时间");
		final String invoice_detail = (String) invoice_data.get("具体信息");
		final String invoice_identity = (String) invoice_data.get("身份证号码");
		final int invoice_region_num = (int) invoice_data.get("region_num");
		final String sql = "insert into invoice values(null,?,?,0,null,?,?,?,?,?,?,?,?,?,?);";
		KeyHolder keyHolder = new GeneratedKeyHolder();
		//返回主键
		getJdbcTemplate().update(new PreparedStatementCreator() {
			@Override
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				PreparedStatement psm = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
				psm.setInt(1, action_id);
				psm.setInt(2, model_id);
				
				psm.setString(3, invoice_type);
				psm.setString(4, invoice_money);
				psm.setString(5, invoice_customer);
				psm.setString(6, invoice_code);
				psm.setString(7, invoice_date);
				psm.setString(8, invoice_time);
				psm.setString(9, invoice_detail);
				psm.setString(10,invoice_identity);
				psm.setInt(11, invoice_region_num);
				psm.setString(12,url);
				return psm;
			}
		},keyHolder);
		return keyHolder.getKey().intValue();
	}

	@Override
	public void addModel(int model_id, Map<String, Object> json_map, String model_register_time,String url) {
		//增加一个新模板
		String sql = "insert into model values(?,?,?,0,?);";
		String json_model = JSON.toJSONString(json_map);
		getJdbcTemplate().update(sql,model_id,json_model,model_register_time,url);
		
	}

	@Override
	public void updateModel(int model_id, String json_model,String url) {
		String sql = "update model set json_model=? model_url = ? where model_id=?";
		getJdbcTemplate().update(sql,json_model,url,model_id);
		
	}


	@Override
	public void finishAddModelAction(int action_id, int model_id,int status) {
		//新增模板跑完后action表信息更新
		//识别完成后，更新action表的信息
		String sql = "update action set model_id = ?,  action_end_time = ?, status=? where action_id = ?";
		String action_end_time = com.hl.util.TimeUtil.getCurrentTime();
		getJdbcTemplate().update(sql,model_id,action_end_time,status,action_id);	
	}
	
	@Override
	public int finishRecognizeAction(int action_id, int invoice_id, int status) {
		//识别完成后，更新action表的信息
		String sql = "update action set invoice_id = ?, status = ?, action_end_time = ? "
				+ "where action_id = ?";
		String action_end_time = com.hl.util.TimeUtil.getCurrentTime();
		return getJdbcTemplate().update(sql,invoice_id,status,action_end_time,action_id);
	}

	@Override
	public void finishUpdateModelAction(Integer action_id, int status) {
		//完成了修改模板的操作
		String sql = "update action set status=? , action_end_time = ? where action_id = ?";
		getJdbcTemplate().update(sql,status,com.hl.util.TimeUtil.getCurrentTime(),action_id);	
	}
	
	@Override
	public void finishDeleteModelAction(Integer action_id, int status) {
		//完成了删除模板的工作
		String sql = "update action set status=? , action_end_time = ? where action_id = ?";
		getJdbcTemplate().update(sql,status,com.hl.util.TimeUtil.getCurrentTime(),action_id);	
	}
	
	@Override
	public int startAction(int action_id) {
		//记录开始跑算法的时间
		String action_run_time = com.hl.util.TimeUtil.getCurrentTime();
		String sql = "update action set action_run_time = ? where action_id = ?";
		return getJdbcTemplate().update(sql,action_run_time,action_id);
	}

	@Override
	public void deleteInvoiceForeginModel(int model_id) {
		//找到全部携带该model_id的invoice，设置外键为null
		String sql = "update invoice set model_id = null where model_id = ?";
		getJdbcTemplate().update(sql,model_id);
	}

	@Override
	public void deleteActionForeginModel(int model_id) {
		//找到全部携带该model_id的action，设置外键为null
		String sql = "update action set model_id = null where model_id = ?";
		getJdbcTemplate().update(sql,model_id);		
	}

	@Override
	public void deleteModel(int model_id) {
		//删除model
		String sql = "delete from model where model_id = ?";
		getJdbcTemplate().update(sql,model_id);
	}

	@Override
	public List<Model> getTwelveModel(Integer start) {
		if(start == 0){
			String sql = "select * from model order by model_id desc LIMIT 12";
			return getJdbcTemplate().query(sql,new ModelRowmapper());
		}else {
			String sql = "select * from model where model_id < ? order by model_id desc LIMIT 12";
			return getJdbcTemplate().query(sql,new ModelRowmapper(),start);
		}
		
	}
	
	class ModelRowmapper implements RowMapper<Model>{
		@Override
		public Model mapRow(ResultSet rs, int rowNum) throws SQLException {
			Model model = new Model();
			model.setJson_model(rs.getString(Const.JSON_MODEL));
			model.setModel_id(rs.getInt(Const.MODEL_ID));
			model.setModel_register_counter(rs.getInt(Const.MODEL_SUCCESS_COUNTER));
			model.setModel_register_time(rs.getString(Const.MODEL_REGISTER_TIME));
			model.setModel_url(rs.getString(Const.MODEL_URL));
			return model;
		}
		
	}

	@Override
	public Map<String, Object> getOneAction(Integer action_id) {
		//得到action_id相关的一些信息
		String sql = "select a.user_id,a.action_start_time,b.user_name,b.company_name "
				+ " from action a, user b where a.user_id = b.user_id and a.action_id = ?";
		return getJdbcTemplate().queryForMap(sql,action_id);
	}

	
	@Override
	public String getModelUrl(int model_id) {
		String sql = "select model_url from model where model_id = ?";
		return getJdbcTemplate().queryForObject(sql, String.class,model_id);
	}

	@Override
	public void deleteAllInvoiceForeginModel() {
		//全部model_id!=null 的invoice，设置外键为null
		String sql = "update invoice set model_id = null";
		getJdbcTemplate().update(sql);
	}

	@Override
	public void deleteAllActionForeginModel() {
		//全部model_id!=null 的action，设置外键为null
		String sql = "update action set model_id = null";
		getJdbcTemplate().update(sql);	
	}

	@Override
	public List<String> getAllModelUrl() {
		//得到全部model_url
		String sql = "select model_url from model";
		return getJdbcTemplate().queryForList(sql,String.class);
	}

	@Override
	public List<Integer> getBiggerModelId(int model_id) {
		//得到全部大于某个model_id的id
		String sql = "select model_id from model where model_id > ? order by model_id asc";
		return getJdbcTemplate().queryForList(sql,Integer.class,model_id);
	}

	@Override
	public void minusModelId(Integer model_id) {
		//model_id减一
		String sql = "update model set model_id = model_id-1 where model_id = ?";
		getJdbcTemplate().update(sql,model_id);
	}

	@Override
	public void clearAllModel() {
		//删除全部model
		String sql = "delete from model";
		getJdbcTemplate().update(sql);
	}

	@Override
	public Map<String, Object> findActionUserNameTime(Integer action_id) {
		//找到user_id和user_name和开始时间
		String sql = "select a.user_id, a.user_name, b.action_start_time from user a,action b "
				+ " where a.user_id = b.user_id and b.action_id = ?";
		return getJdbcTemplate().queryForMap(sql,action_id);
	}

	@Override
	public void plusModelSuccess(Integer model_id) {
		//识别成功次数加一
		String sql = "update model set model_success_counter = model_success_counter + 1  where model_id = ?";
		getJdbcTemplate().update(sql,model_id);	
	}

	@Override
	public void updateModelUrl(String url,String changed_url) {
		String sql = "update model set model_url = ? where model_url = ?";
		getJdbcTemplate().update(sql,changed_url,url);
	}



	



}