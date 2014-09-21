package model;

public class FXStartModel {
	
	private boolean autoModeFlag = true;
	
	private String selectedModel = null;
		
	/**
	 * @return the autoModeFlag
	 */
	public boolean isAutoModeFlag() {
		return autoModeFlag;
	}

	/**
	 * @param autoModeFlag the autoModeFlag to set
	 */
	public void setAutoModeFlag(boolean autoModeFlag) {
		this.autoModeFlag = autoModeFlag;
	}

	/**
	 * @return the selectedModel
	 */
	public String getSelectedModel() {
		return selectedModel;
	}

	/**
	 * @param selectedModel the selectedModel to set
	 */
	public void setSelectedModel(String selectedModel) {
		this.selectedModel = selectedModel;
	}
}
