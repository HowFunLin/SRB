package com.heepay.model;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * <p>
 * UserBind
 * </p>
 *
 * @author qy
 */
@Data
public class NotifyVo implements Serializable {
	
	private static final long serialVersionUID = 1L;


	private String notifyUrl;

	private Map<String, Object> paramMap;

	public NotifyVo() {}

	public NotifyVo(String notifyUrl, Map<String, Object> paramMap) {
		this.notifyUrl = notifyUrl;
		this.paramMap = paramMap;
	}
}

