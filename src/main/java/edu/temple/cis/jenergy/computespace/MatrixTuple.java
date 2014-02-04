package edu.temple.cis.jenergy.computespace;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MatrixTuple {
	@Id
	public String id;
	@Column(name = "data")
	public double[][] data;
	@Column
	public int startRow;
	@Column
	public int numCols;
	@Column
	public int numRows;

	// for creating a tuple that starts from zero matrix offset
	public MatrixTuple(String id, double[][] data) {
		this.data = data;
		this.startRow = 0;
		this.numRows = data.length;
		this.numCols = data[0].length;
		this.id = id;
	}

	// for creating a tuple that starts from specified matrix offset
	public MatrixTuple(String id, double[][] data, int start) {
		this.data = data;
		this.startRow = start;
		this.numRows = data.length;
		this.numCols = data[0].length;
		this.id = id;
	}
	
	public MatrixTuple(){
		
	}

	public String getRequestNum() {
		return this.id.split("_")[1];
	}

}
