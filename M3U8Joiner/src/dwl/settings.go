package dwl

type Settings struct {
	Threads      int
	ErrorRepeat  int
	DownloadPath string

	Useragent string
	Cookies   string
}

func (m *Manager) GetSettings() *Settings {
	sets := new(Settings)
	sets.Threads = m.Settings.Threads
	sets.ErrorRepeat = m.Settings.ErrorRepeat
	sets.DownloadPath = m.Settings.DownloadPath
	sets.Useragent = m.Settings.Useragent
	sets.Cookies = m.Settings.Cookies
	return sets
}

func (m *Manager) SetSettingsDownloadPath(val string) {
	m.Settings.DownloadPath = val
}

func (m *Manager) SetSettingsUseragent(val string) {
	m.Settings.Useragent = val
}

func (m *Manager) SetSettingsCookies(val string) {
	m.Settings.Cookies = val
}

func (m *Manager) SetSettingsThreads(val int) {
	m.Settings.Threads = val
}

func (m *Manager) SetSettingsErrorRepeat(val int) {
	m.Settings.ErrorRepeat = val
}

func (m *Manager) SaveSettings() string {
	err := m.saveSettings()
	if err != nil {
		return err.Error()
	}
	return ""
}