package omar.expenses.persist.model;

import android.annotation.SuppressLint;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import omar.expenses.persist.annotation.Column;
import omar.expenses.persist.annotation.Id;
import omar.expenses.persist.annotation.Table;

@SuppressLint("SimpleDateFormat")
@Table(name="expenses", version=1)
public class Expense implements BaseEntity {

		@Id
		@Column(name = "id", version = 1)
		private Integer	id;

		@Column(name = "tipo", version = 1)
		private String	tipo;

		@Column(name = "valor", version = 1)
		private Double valor;
		
		@Column(name = "data", version = 1)
		private Date data;
		
		@Column(name = "despesa", version = 1)
		private Boolean despesa;
		
		@Column(name = "descricao", version = 1)
		private String descricao;

		public Integer getId() {
			return id;
		}

		public void setId(Integer id) {
			this.id = id;
		}

		public String getTipo() {
			return tipo;
		}

		public void setTipo(String tipo) {
			this.tipo = tipo;
		}

		public Double getValor() {
			return valor;
		}

		public void setValor(Double valor) {
			this.valor = valor;
		}

		/*public Date getActualData() {
			return Expense.getDataFromValue(this.data);
		}

		public void setActualData(Date data) {
			this.data = Expense.getDataValueFrom(data);
		}*/
		
		public Date getData() {
			return data;
		}

		public void setData(Date data) {
			this.data = data;
		}

		public Boolean getDespesa() {
			return despesa;
		}

		public void setDespesa(Boolean despesa) {
			this.despesa = despesa;
		}

		public String getDescricao() {
			return descricao;
		}

		public void setDescricao(String descricao) {
			this.descricao = descricao;
		}
		
		///////////
		public static Date getDataFromValue(Long dataValue) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			try {
				return df.parse(dataValue.toString());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			return null;
		}
		public static Long getDataValueFrom(Date date) {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
			return Long.valueOf(df.format(date));
		}
		
		@Override
		public String toString() {
			String s = "Expense [" + id.toString() + "]";
			s += "\n   tipo = " + tipo;
			s += "\n   valor = " + valor.toString();
			if (data != null)
				s += "\n   data = " + data.toString();
			else
				s += "\n   data = NULL";
			s += "\n   descricao = " + descricao;
			s += "\n   eh despesa = " + despesa.toString();
			return s;
		}
}
