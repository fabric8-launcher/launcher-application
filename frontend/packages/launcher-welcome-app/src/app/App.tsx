import { Brand, Button, ButtonVariant, Nav, NavItem, NavList, Page, PageHeader, PageSection, PageSectionVariants, PageSidebar, Text, TextContent, Toolbar, ToolbarGroup, ToolbarItem } from '@patternfly/react-core';
import { CloudIcon, CodeIcon, CogIcon, ScreenIcon, ServicesIcon } from '@patternfly/react-icons';
import { global_breakpoint_md as breakpointMd } from '@patternfly/react-tokens';
import * as _ from 'lodash';
import * as React from 'react';
import logo from '../assets/logo/RHD-logo.svg';
import { getLocationAbsoluteUrl } from '../shared/utils/Locations';
import { checkNotNull } from '../shared/utils/Preconditions';
import './App.scss';
import { Capability } from './capabilities/Capability';
import appConfig from './config/appConfig';
import { Part } from './config/AppDefinition';
import capabilitiesConfig from './config/capabilitiesConfig';
import { CloudDeploymentInfo } from './infos/CloudDeploymentInfo';
import { CodeBaseInfo } from './infos/CodeBaseInfo';
import { PartInfo } from './infos/PartInfo';

const appDefinition = checkNotNull(appConfig.definition, 'appConfig.definition');
const backendPart = appDefinition.parts.find(t => t.extra.category === 'backend') as Part;
const frontendPart = appDefinition.parts.find(t => t.extra.category === 'frontend') as Part;
const capabilities = [...(backendPart ? backendPart.capabilities : [])];
const capabilityDefinitionByModule = _.keyBy(capabilities, 'module');

export default class App extends React.Component<{}, { isNavOpen: boolean }> {

  constructor(props: {}) {
    super(props);
    const isNavOpen = typeof window !== 'undefined' && window.innerWidth >= parseInt(breakpointMd.value, 10);
    this.state = {
      isNavOpen,
    };
  }

  public render() {

    const PageNav = (
      <Nav onSelect={this.onNavSelect} onToggle={this.onNavToggle} aria-label="Nav">
        <NavList>
          <NavItem to={`#cloud-deployment-info`}>
            <CloudIcon className="with-text" /> Cloud Deployment
          </NavItem>
          {appConfig.sourceRepository && (
            <NavItem to={`#codebase-info`}>
              <CodeIcon className="with-text" /> Codebase
            </NavItem>
          )}
          {frontendPart && (
            <NavItem to={`#frontend-tier-info`}>
              <ScreenIcon className="with-text" /> Frontend
            </NavItem>
          )}
          {backendPart && (
            <NavItem to={`#backend-tier-info`}>
              <ServicesIcon className="with-text" /> Backend
            </NavItem>
          )}
          {_.values(capabilitiesConfig).filter(this.showCapability).map(c => (
            <NavItem key={c.module} to={`#${c.module}-capability`}>
              {c.icon} {c.name}
            </NavItem>
          ))}
        </NavList>
      </Nav>
    );
    const PageToolbar = (
      <Toolbar>
        <ToolbarGroup>
          <ToolbarItem>
            <Button id="nav-toggle" aria-label="Overflow actions" variant={ButtonVariant.plain}>
              <CogIcon />
            </Button>
          </ToolbarItem>
        </ToolbarGroup>
      </Toolbar>
    );

    const Header = (
      <PageHeader
        logo={<Brand src={logo} alt="Red Hat" />}
        toolbar={PageToolbar}
        showNavToggle
        onNavToggle={this.onNavToggle}
      />
    );

    const Sidebar = <PageSidebar nav={PageNav} isNavOpen={this.state.isNavOpen} />;

    return (
      <React.Fragment>
        <Page header={Header} sidebar={Sidebar}>
          <PageSection variant={PageSectionVariants.light}>
            <TextContent>
              <Text component="h1">Here we go.</Text>
              <Text component="p">
                Congratulations; we've together built a working system of application code and services running on the OpenShift platform.
                Everything is hooked together for you to bring your ideas to life.
              </Text>
            </TextContent>
          </PageSection>
          <PageSection>
            <CloudDeploymentInfo
              applicationName={appDefinition.application}
              applicationUrl={getLocationAbsoluteUrl('')}
              openshiftConsoleUrl={appConfig.openshiftConsoleUrl!}
            />
            {appConfig.sourceRepository && (
              <CodeBaseInfo sourceRepository={appConfig.sourceRepository} />
            )}
            {frontendPart && (
              <PartInfo {...this.createPartInfoProps(frontendPart)} />
            )}
            {backendPart && (
              <PartInfo {...this.createPartInfoProps(backendPart)} />
            )}
            {_.values(capabilitiesConfig).filter(this.showCapability).map(c => {
              const capabilityDefinition = capabilityDefinitionByModule[c.module] || {};
              const props = { module: c.module, ...capabilityDefinition.props, ...capabilityDefinition.extra };
              return (
                <Capability {...props} key={c.module} />
              );
            })}
          </PageSection>
        </Page>
      </React.Fragment>
    );
  }

  private createPartInfoProps = (part: Part) => {
    return {
      subfolderName: part.subFolderName,
      category: part.extra.category,
      runtimeInfo: { ...part.extra.runtimeInfo! },
    };
  };

  private showCapability = (capability: { module: string, requireDefinition: boolean }) => {
    return !capability.requireDefinition || !!capabilityDefinitionByModule[capability.module];
  };

  private onNavSelect = (result: any) => {
  };

  private onNavToggle = () => {
    this.setState({
      isNavOpen: !this.state.isNavOpen
    });
  };
}
