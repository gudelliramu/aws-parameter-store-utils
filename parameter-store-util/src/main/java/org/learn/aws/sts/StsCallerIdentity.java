package org.learn.aws.sts;

public class StsCallerIdentity {

	private String userId;

	private String account;

	private String arn;

	public StsCallerIdentity() {
	}

	public StsCallerIdentity(String userId, String account, String arn) {
		this.userId = userId;
		this.account = account;
		this.arn = arn;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getArn() {
		return arn;
	}

	public void setArn(String arn) {
		this.arn = arn;
	}

	public static Builder builder() {
		return new Builder();
	}

	static class Builder {

		private String userId;

		private String account;

		private String arn;

		public Builder userId(String userId) {
			this.userId = userId;
			return this;
		}

		public Builder account(String account) {
			this.account = account;
			return this;
		}

		public Builder arn(String arn) {
			this.arn = arn;
			return this;
		}

		public StsCallerIdentity build() {
			return new StsCallerIdentity(this.userId, this.account, this.arn);
		}

		
		
	}

}
