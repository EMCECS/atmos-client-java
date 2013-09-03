// Copyright (c) 2012, EMC Corporation.
// Redistribution and use in source and binary forms, with or without modification, 
// are permitted provided that the following conditions are met:
//
//     + Redistributions of source code must retain the above copyright notice, 
//       this list of conditions and the following disclaimer.
//     + Redistributions in binary form must reproduce the above copyright 
//       notice, this list of conditions and the following disclaimer in the 
//       documentation and/or other materials provided with the distribution.
//     + The name of EMC Corporation may not be used to endorse or promote 
//       products derived from this software without specific prior written 
//       permission.
//
//      THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
//      "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
//      TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
//      PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS 
//      BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
//      CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
//      SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
//      INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
//      CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
//      ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
//      POSSIBILITY OF SUCH DAMAGE.
package com.emc.atmos.sync.plugins;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.util.Assert;

/**
 * This is an extension of the filesystem source that reads its list of 
 * files to transfer from a database SELECT query.
 * 
 * Note that this source currently only supports Spring configuration.
 *
 */
public class DbListFilesystemSource extends FilesystemSource implements InitializingBean {
	private DataSource dataSource;
	private String selectQuery;
	private List<String> metadataColumns;
	private String filenameColumn;
	
	@Override
	public void run() {
		running = true;
		initQueue();
		
		JdbcTemplate tmpl = new JdbcTemplate(dataSource);
		
		SqlRowSet rs = tmpl.queryForRowSet(selectQuery);
		
		while(rs.next()) {
			File f = new File(rs.getString(filenameColumn));
			ReadFileTask t = new ReadFileTask(f);
			
			if(metadataColumns != null) {
				Map<String, String> extraMetadata = new HashMap<String, String>();
				for(String colName : metadataColumns) {
					String value = rs.getString(colName);
					if(value == null) {
						continue;
					}
					extraMetadata.put(colName, value);
				}
				t.setExtraMetadata(extraMetadata);
			}
			
			if(f.isDirectory()) {
				submitCrawlTask(t);
			} else {
				submitTransferTask(t);
			}
		}

		runQueue();
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.hasText(filenameColumn, 
				"The property 'filenameColumn' is required");
		Assert.hasText(selectQuery, "The property 'selectQuery' is required.");
		Assert.notNull(dataSource, "The property 'dataSource' is required.");
		setUseAbsolutePath(true);
	}

	/**
	 * @return the dataSource
	 */
	public DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * @param dataSource the dataSource to set
	 */
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * @return the selectQuery
	 */
	public String getSelectQuery() {
		return selectQuery;
	}

	/**
	 * @param selectQuery the selectQuery to set
	 */
	public void setSelectQuery(String selectQuery) {
		this.selectQuery = selectQuery;
	}

	/**
	 * @return the metadataColumns
	 */
	public List<String> getMetadataColumns() {
		return metadataColumns;
	}

	/**
	 * @param metadataColumns the metadataColumns to set
	 */
	public void setMetadataColumns(List<String> metadataColumns) {
		this.metadataColumns = metadataColumns;
	}

	/**
	 * @return the filenameColumn
	 */
	public String getFilenameColumn() {
		return filenameColumn;
	}

	/**
	 * @param filenameColumn the filenameColumn to set
	 */
	public void setFilenameColumn(String filenameColumn) {
		this.filenameColumn = filenameColumn;
	}
}
