package com.mehamood.ir.project;

public class Document {

	
		private String docno;
		private String content;

		public Document(String docno, String content) {
			this.docno = docno;
			this.content = content;
		}

		public String getDocno() {
			return docno;
		}

		public String getContent() {
			return content;
		}
	
}
