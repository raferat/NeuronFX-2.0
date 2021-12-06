
package cz.stv.neuronnetworkfromnet;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class Matrix implements Serializable, Cloneable
{
	public final int rows;
  
	public final int cols;
  
	public final double[][] data;
  
	public Matrix(int rows, int cols)
	{
		this.rows = rows;
		this.cols = cols;
		this.data = new double[rows][cols];
	}
	
	public Matrix(double[][] data)
	{
		this.rows = data.length;
		this.cols = data[0].length;
		this.data = new double[rows][cols];
		// Simple copy of a 2-dimensional double array
		for(int r = 0; r < rows; r++)
		{
			for(int c = 0; c < cols; c++)
			{
				this.data[r][c] = data[r][c];
			}
		}
	}
	
	public Matrix randomize()
	{
		return randomize(ThreadLocalRandom.current());
	}

	
	public Matrix randomize(final Random rand)
	{
		return map(new MatFunc()
		{
			@Override
			public double perform(double val, int r, int c)
			{
				return rand.nextDouble() * 2 - 1;
			}
		});
	}
	
	public Matrix add(final Matrix mat)
	{
		return new Matrix(data).map(new MatFunc()
		{
			@Override
			public double perform(double val, int r, int c)
			{
				return val + mat.data[r][c];
			}
		});
	}
	
	public Matrix add(final double v)
	{
		return new Matrix(data).map(new MatFunc()
		{
			@Override
			public double perform(double val, int r, int c)
			{
				return val + v;
			}
		});
	}
	
	public Matrix subtract(final Matrix mat)
	{
		return new Matrix(data).map(new MatFunc()
		{
			@Override
			public double perform(double val, int r, int c)
			{
				return val - mat.data[r][c];
			}
		});
	}
	
	public Matrix subtract(final double v)
	{
		return new Matrix(data).map(new MatFunc()
		{
			@Override
			public double perform(double val, int r, int c)
			{
				return val - v;
			}
		});
	}
	
	public Matrix transpose()
	{
		return new Matrix(cols, rows).map(new MatFunc()
		{
			@Override
			public double perform(double val, int r, int c)
			{
				return data[c][r];
			}
		});
	}
	
	public Matrix mult(final double scl)
	{
		return new Matrix(data).map(new MatFunc()
		{
			@Override
			public double perform(double val, int r, int c)
			{
				return val * scl;
			}
		});
	}

	public Matrix elementMult(final Matrix mat)
	{
		return new Matrix(data).map(new MatFunc()
		{
			@Override
			public double perform(double val, int r, int c)
			{
				return val * mat.data[r][c];
			}
		});
	}

	public Matrix mult(final Matrix mat)
	{
		if(cols != mat.rows) throw new MatrixException("Rows don't match columns");
		
		return new Matrix(rows, mat.cols).map(new MatFunc()
		{
			@Override
			public double perform(double val, int r, int c)
			{
				double sum = 0;
				for(int i = 0; i < cols; i++)
				{
					sum += data[r][i] * mat.data[i][c];
				}
				return sum;
			}
		});
	}
	
	public Matrix map(MatFunc func)
	{
		for(int r = 0; r < rows; r++)
		{
			for(int c = 0; c < cols; c++)
			{
				data[r][c] = func.perform(data[r][c], r, c);
			}
		}
		return this;
	}
	
	public double[] toArray()
	{
		double[] arr = new double[rows * cols];
		for(int r = 0; r < rows; r++)
		{
			for(int c = 0; c < cols; c++)
			{
				arr[c + r * cols] = data[r][c];
			}
		}
		return arr;
	}
	
	public double[] getColumn(int col)
	{
		double[] column = new double[rows];
		for(int i = 0; i < rows; i++)
		{
			column[i] = data[i][col];
		}
		return column;
	}

	public Matrix clone()
	{
		return new Matrix(data);
	}
	
	public String toArrayString()
	{
		return Arrays.deepToString(data);
	}
	
	public String toString()
	{		
		StringBuilder sb = new StringBuilder();
		for(int r = 0; r < rows; r++)
		{
			sb.append("[");
			for(int c = 0; c < cols; c++)
			{
				sb.append(data[r][c]);

				if(c < cols - 1) sb.append(", ");
			}
			
			sb.append(']');
			if(r < rows - 1) sb.append('\n');
		}
		return sb.toString();
	}
	
	public static Matrix fromArray(double[] arr)
	{
		Matrix mat = new Matrix(arr.length, 1);
		for(int i = 0; i < arr.length; i++)
		{
			mat.data[i][0] = arr[i];
		}
		return mat;
	}
	
	public interface MatFunc
	{
		public double perform(double val, int r, int c);
	}
	
	public static MatFunc SIGMOID = new MatFunc()
	{
		@Override
		public double perform(double val, int r, int c)
		{
			return 1 / (1 + Math.exp(-val));
		}
	};
	
	public static MatFunc SIGMOID_DERIVATIVE = new MatFunc()
	{
		@Override
		public double perform(double val, int r, int c)
		{
			return val * (1 - val);
		}
	};
	
	public static MatFunc TANH = new MatFunc()
	{
		@Override
		public double perform(double val, int r, int c)
		{
			return Math.tanh(val);
		}
	};
	
	public static MatFunc TANH_DERIVATIVE = new MatFunc()
	{
		@Override
		public double perform(double val, int r, int c)
		{
			return 1 - val * val;
		}
	};
	
	private static final long serialVersionUID = 3107367440033528127L;
}
