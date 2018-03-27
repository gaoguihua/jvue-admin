package net.ccfish.jvue.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import net.ccfish.common.jpa.JpaRestrictions;
import net.ccfish.common.jpa.SearchCriteria;
import net.ccfish.jvue.model.JvueMenu;
import net.ccfish.jvue.model.JvueModule;
import net.ccfish.jvue.model.JvueRole;
import net.ccfish.jvue.model.JvueRoleApi;
import net.ccfish.jvue.model.JvueRoleMenu;
import net.ccfish.jvue.model.JvueSegment;
import net.ccfish.jvue.repository.JvueMenuRepository;
import net.ccfish.jvue.repository.JvueModuleRepository;
import net.ccfish.jvue.repository.JvueRoleApiRepository;
import net.ccfish.jvue.repository.JvueRoleMenuRepository;
import net.ccfish.jvue.repository.JvueRoleRepository;
import net.ccfish.jvue.repository.JvueSegmentRepository;
import net.ccfish.jvue.service.JvueRoleService;
import net.ccfish.jvue.service.JvueSegmentService;
import net.ccfish.jvue.vm.ModuleAndMenus;

/**
 * Generated by Spring Data Generator on 31/01/2018
 */
@Service
@Transactional
public class JvueRoleServiceImpl implements JvueRoleService {

    private JvueRoleRepository jvueRoleRepository;

    @Autowired
    private JvueMenuRepository jvueMenuRepository;

    @Autowired
    private JvueModuleRepository jvueModuleRepository;
    
    @Autowired
    private JvueRoleApiRepository jvueRoleApiRepository;
    
    @Autowired
    private JvueRoleMenuRepository jvueRoleMenuRepository;

    @Autowired
    private JvueSegmentRepository jvueSegmentRepository;
    
    @Autowired
    private JvueSegmentService segmentService;

    @Autowired
    public JvueRoleServiceImpl(JvueRoleRepository jvueRoleRepository) {
        this.jvueRoleRepository = jvueRoleRepository;
    }

    @Override
    public JpaRepository<JvueRole, Integer> jpaRepository() {
        return this.jvueRoleRepository;
    }

    @Override
    @Cacheable(value = "role-menu", key = "#roles")
    public ModuleAndMenus findModuleAndMenu(List<Integer> roles) {

        //TODO 根据权限查询对应的菜单和modules/segments
        ModuleAndMenus moduleAndMenus = new ModuleAndMenus();
        SearchCriteria<JvueRoleMenu> roleMenuCriterias = new SearchCriteria<>();
        roleMenuCriterias.add(JpaRestrictions.in("role.id", roles, false));
        
        List<JvueRoleMenu> roleMenus = jvueRoleMenuRepository.findAll(roleMenuCriterias);
        List<JvueMenu>  menus = roleMenus.stream().map(roleMenu -> roleMenu.getMenu()).collect(Collectors.toList());

        List<Integer> moduleIds = menus.stream().map(menu -> menu.getModuleId()).distinct()
                .collect(Collectors.toList());

        List<JvueModule> modules = jvueModuleRepository.findAllById(moduleIds);

        menus.forEach(menu -> {
            List<JvueSegment> segments = segmentService.findByMenu(menu.getId());
            menu.setSegments(segments);
        });
        
        moduleAndMenus.setMenus(menus);
        moduleAndMenus.setModules(modules);

        return moduleAndMenus;
    }

    /* (non-Javadoc)
     * @see net.ccfish.jvue.service.JvueRoleService#updateEnabled(java.lang.Integer, byte)
     */
    @Override
    public JvueRole updateEnabled(Integer id, byte enabled) {
        
        Optional<JvueRole> jvueRoleOptional = jvueRoleRepository.findById(id);
                
        if (jvueRoleOptional.isPresent()) {
            JvueRole jvueRole = jvueRoleOptional.get();
            jvueRole.setEnabled(enabled);
            return jvueRoleRepository.save(jvueRole);
        }

        return null;
    }

    /* (non-Javadoc)
     * @see net.ccfish.jvue.service.JvueRoleService#rolesByApi(java.lang.Integer)
     */
    @Override
    @Cacheable(value = "role-api", key = "#apiId")
    public List<Integer> getRolesByApi(Integer apiId) {
        SearchCriteria<JvueRoleApi> roleApiCriteria = new SearchCriteria<>();
        roleApiCriteria.add(JpaRestrictions.eq("api.apiId", apiId, false));
        
        List<JvueRoleApi> jvueRoleApis = jvueRoleApiRepository.findAll(roleApiCriteria);
        return jvueRoleApis.stream().map(roleApi -> roleApi.getRole().getId()).collect(Collectors.toList());
    }


    // TODO
    // 开发/调试模式下，开发者等特殊角色可以无视enabled直接启用，方便调试画面
    
}
