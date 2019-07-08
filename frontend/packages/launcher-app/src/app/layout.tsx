import {
  Brand,
  Dropdown,
  DropdownItem,
  DropdownToggle,
  Page,
  PageHeader,
  Toolbar,
  ToolbarGroup,
  ToolbarItem
} from '@patternfly/react-core';
import * as React from 'react';
import { useState } from 'react';
import logo from './assets/logo/RHD-logo.svg';
import style from './layout.module.scss';
import { createRouterLink, useRouter } from '../router/use-router';
import { ReactNode } from 'react';
import { useAuthenticationApi } from '../auth/auth-context';

export function Layout(props: { children: React.ReactNode }) {
  const [isUserDropdownOpen, setIsUserDropdownOpen] = useState(false);
  const router = useRouter();
  const rootLink = createRouterLink(router, '/');
  const auth = useAuthenticationApi();
  const logout = () => {
    auth.logout();
  };
  let PageToolbar: ReactNode;
  if (auth.enabled && auth.user) {
    const userDropdownItems = [
      <DropdownItem onClick={logout} key="logout">Logout</DropdownItem>,
    ];
    const accountManagementLink = auth.getAccountManagementLink();
    if (accountManagementLink) {
      userDropdownItems.unshift(
        <DropdownItem component="a" href={accountManagementLink} target="_blank" key="manage">Manage Account</DropdownItem>
      );
    }
    PageToolbar = auth.enabled && auth.user && (
      <Toolbar>
        <ToolbarGroup>
          <ToolbarItem>
            <Dropdown
              isPlain
              position="right"
              onSelect={() => setIsUserDropdownOpen((prev) => !prev)}
              isOpen={isUserDropdownOpen}
              toggle={<DropdownToggle onToggle={(val: boolean) => setIsUserDropdownOpen(val)}>{auth.user.userPreferredName}</DropdownToggle>}
              dropdownItems={userDropdownItems}
            />
          </ToolbarItem>
        </ToolbarGroup>
      </Toolbar>
    );
  }

  const Header = (
    <PageHeader
      logo={<Brand src={logo} alt="Red Hat" className={style.brand} onClick={rootLink.onClick}/>}
      logoProps={{ href: process.env.PUBLIC_URL }}
      toolbar={PageToolbar}
      className={style.header}
    />
  );

  return (
    <React.Fragment>
      <Page header={Header}>
        {props.children}
      </Page>
    </React.Fragment>
  );
}
