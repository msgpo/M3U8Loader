package dwl

import (
	"dwl/list"
	"dwl/load"
	"dwl/settings"
	"encoding/json"
	"fmt"
	"io/ioutil"
	"path/filepath"
	"sort"
)

const managerConfigFile = "dwl.cfg"

func (m *Manager) loadConfig() error {
	m.readSettings()
	files, err := ioutil.ReadDir(m.saveDir)
	if err != nil {
		return err
	}

	for _, f := range files {
		if filepath.Ext(f.Name()) == ".lst" {
			ldr, err := m.readLoader(filepath.Join(m.saveDir, f.Name()))
			if err != nil {
				fmt.Println("Error load:", f.Name(), err)
			}
			if ldr != nil {
				m.loaders = append(m.loaders, ldr)
			}
		}
	}

	sort.Slice(m.loaders, func(i, j int) bool {
		return m.loaders[i].GetList().Name < m.loaders[j].GetList().Name
	})

	return nil
}

func (m *Manager) saveSettings() error {
	if m.Settings == nil {
		m.Settings = settings.NewSettings()
	}

	buf, err := json.MarshalIndent(m.Settings, "", " ")
	if err != nil {
		return err
	}
	return ioutil.WriteFile(filepath.Join(m.saveDir, managerConfigFile), buf, 0666)
}

func (m *Manager) getLoaderCfgPath(loader *load.Loader) string {
	return filepath.Join(m.saveDir, loader.GetList().Name) + ".lst"
}

func (m *Manager) saveLoader(loader *load.Loader) error {
	if m.saveDir == "" || loader.GetList().Name == "" {
		return nil
	}
	fn := m.getLoaderCfgPath(loader)

	list := loader.GetList()
	buf, err := json.MarshalIndent(list, "", " ")
	if err != nil {
		return err
	}
	return ioutil.WriteFile(fn, []byte(buf), 0666)
}

func (m *Manager) readSettings() {
	if m.Settings == nil {
		m.Settings = new(settings.Settings)
	}
	if buf, err := ioutil.ReadFile(filepath.Join(m.saveDir, managerConfigFile)); err == nil {
		if json.Unmarshal(buf, m.Settings) == nil {
			return
		}
	}
	m.Settings = settings.NewSettings()
	m.saveSettings()
}

func (m *Manager) readLoader(fn string) (*load.Loader, error) {
	buf, err := ioutil.ReadFile(fn)
	if err != nil {
		return nil, err
	}
	var list *list.List = new(list.List)
	err = json.Unmarshal(buf, list)
	if err != nil {
		return nil, err
	}

	if list != nil && list.Len() > 0 {
		//при открытии сохранения, могут быть дыры, тогда нужно загружать всё с дыры
		find := false
		for n := 0; n < list.Len(); n++ {
			itm := list.Get(n)
			itm.IsLoading = false                                          //not loading on open
			itm.AverSpeed = 0                                              //reset speed
			if !find && (!itm.IsComplete || itm.Size == 0) && itm.IsLoad { //found a hole
				find = true
				itm.IsComplete = false
				itm.Size = 0
				continue
			}
			if find { //closed a hole
				itm.IsComplete = false
				itm.Size = 0
			}
		}
	}

	return load.NewLoader(m.Settings, list), nil
}

func (m *Manager) update(loader *load.Loader) {
	m.saveLoader(loader)
}