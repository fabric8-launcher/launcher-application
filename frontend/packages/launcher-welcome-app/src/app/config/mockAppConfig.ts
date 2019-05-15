import { AppDefinition } from './AppDefinition';

const mockAppDefinition = {
  application: 'wish-list',
  parts: [
    {
      subFolderName: 'backend',
      shared: {
        runtime: {
          name: 'vertx',
        },
        maven: {
          version: '1.0.0-SNAPSHOT',
          artifactId: 'booster',
          groupId: 'io.openshift.booster'
        }
      },
      extra: {
        category: 'backend',
        runtimeInfo: {
          image: 'registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift',
          enumInfo: {
            id: 'vertx',
            name: 'Vert.x',
            // tslint:disable-next-line
            icon: 'data:image/svg+xml;charset=utf8,%3Csvg xmlns=\'http://www.w3.org/2000/svg\' viewBox=\'0 0 640 280\'%3E%3Cpath fill=\'%23022B37\' d=\'M107 170.8L67.7 72H46.9L100 204h13.9L167 72h-20.4zm64 33.2h80v-20h-61v-37h60v-19h-60V91h61V72h-80zm180.1-90.7c0-21-14.4-42.3-43.1-42.3h-48v133h19V91h29.1c16.1 0 24 11.1 24 22.4 0 11.5-7.9 22.6-24 22.6H286v9.6l48 58.4h24.7L317 154c22.6-4 34.1-22 34.1-40.7m56.4 90.7v-1c0-6 1.7-11.7 4.5-16.6V91h39V71h-99v20h41v113h14.5z\'/%3E%3Cpath fill=\'%23623C94\' d=\'M458 203c0-9.9-8.1-18-18-18s-18 8.1-18 18 8.1 18 18 18 18-8.1 18-18M577.4 72h-23.2l-27.5 37.8L499.1 72h-40.4c12.1 16 33.6 46.8 47.8 66.3l-37 50.9c2 4.2 3.1 8.9 3.1 13.8v1H499l95.2-132h-16.8zm-19.7 81.5l-20.1 27.9 16.5 22.6h40.2c-9.6-13.7-24-33.3-36.6-50.5z\'/%3E%3C/svg%3E',
            description: 'A tool-kit for building reactive applications on the JVM.',
            metadata: {
              language: 'java'
            }
          },
          service: 'wiyqsh-list-service',
          route: 'wiyqsh-list-service',
        },
      },
      capabilities: [
        {
          module: 'database',
          props: {
            databaseType: 'postgresql'
          },
          extra: {
            databaseImage: 'postgresql',
            databaseService: 'wish-list-database',
            sourceMapping: {
              dbEndpoint: 'src/main/java/io/openshift/booster/database/CrudApplication.java'
            }
          }
        },
        {
          module: 'rest',
          props: {
            sourceRepository: {
              provider: 'github',
              url: 'https://github.com/org/name.git'
            }
          },
          extra: {
            sourceMapping: {
              greetingEndpoint: 'src/main/java/io/openshift/booster/http/HttpApplication.java'
            }
          }
        },
        {
          module: 'welcome',
          props: {},
          extra: {}
        }
      ]
    },
    {
      shared: {
        runtime: {
          name: 'react'
        }
      },
      extra: {
        category: 'frontend',
        runtimeInfo: {
          image: 'nodeshift/centos7-s2i-web-app',
          enumInfo: {
            id: 'react',
            name: 'React',
            description: 'A reactive JavaScript framework for building user interfaces.',
            // tslint:disable-next-line
            icon: `data:image/svg+xml,<%3fxml version='1.0' encoding='utf-8'%3f> <!DOCTYPE svg PUBLIC '-//W3C//DTD SVG 1.1//EN' 'http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd'> <svg version='1.1' id='Layer_2_1_' xmlns='http://www.w3.org/2000/svg' xmlns:xlink='http://www.w3.org/1999/xlink' x='0px' y='0px' viewBox='0 0 841.9 595.3' enable-background='new 0 0 841.9 595.3' xml:space='preserve'> <g> <path fill='%2361DAFB' d='M666.3%2c296.5c0-32.5-40.7-63.3-103.1-82.4c14.4-63.6%2c8-114.2-20.2-130.4c-6.5-3.8-14.1-5.6-22.4-5.6v22.3 c4.6%2c0%2c8.3%2c0.9%2c11.4%2c2.6c13.6%2c7.8%2c19.5%2c37.5%2c14.9%2c75.7c-1.1%2c9.4-2.9%2c19.3-5.1%2c29.4c-19.6-4.8-41-8.5-63.5-10.9 c-13.5-18.5-27.5-35.3-41.6-50c32.6-30.3%2c63.2-46.9%2c84-46.9l0-22.3c0%2c0%2c0%2c0%2c0%2c0c-27.5%2c0-63.5%2c19.6-99.9%2c53.6 c-36.4-33.8-72.4-53.2-99.9-53.2v22.3c20.7%2c0%2c51.4%2c16.5%2c84%2c46.6c-14%2c14.7-28%2c31.4-41.3%2c49.9c-22.6%2c2.4-44%2c6.1-63.6%2c11 c-2.3-10-4-19.7-5.2-29c-4.7-38.2%2c1.1-67.9%2c14.6-75.8c3-1.8%2c6.9-2.6%2c11.5-2.6l0-22.3c0%2c0%2c0%2c0%2c0%2c0c-8.4%2c0-16%2c1.8-22.6%2c5.6 c-28.1%2c16.2-34.4%2c66.7-19.9%2c130.1c-62.2%2c19.2-102.7%2c49.9-102.7%2c82.3c0%2c32.5%2c40.7%2c63.3%2c103.1%2c82.4c-14.4%2c63.6-8%2c114.2%2c20.2%2c130.4 c6.5%2c3.8%2c14.1%2c5.6%2c22.5%2c5.6c27.5%2c0%2c63.5-19.6%2c99.9-53.6c36.4%2c33.8%2c72.4%2c53.2%2c99.9%2c53.2c8.4%2c0%2c16-1.8%2c22.6-5.6 c28.1-16.2%2c34.4-66.7%2c19.9-130.1C625.8%2c359.7%2c666.3%2c328.9%2c666.3%2c296.5z M536.1%2c229.8c-3.7%2c12.9-8.3%2c26.2-13.5%2c39.5 c-4.1-8-8.4-16-13.1-24c-4.6-8-9.5-15.8-14.4-23.4C509.3%2c224%2c523%2c226.6%2c536.1%2c229.8z M490.3%2c336.3c-7.8%2c13.5-15.8%2c26.3-24.1%2c38.2 c-14.9%2c1.3-30%2c2-45.2%2c2c-15.1%2c0-30.2-0.7-45-1.9c-8.3-11.9-16.4-24.6-24.2-38c-7.6-13.1-14.5-26.4-20.8-39.8 c6.2-13.4%2c13.2-26.8%2c20.7-39.9c7.8-13.5%2c15.8-26.3%2c24.1-38.2c14.9-1.3%2c30-2%2c45.2-2c15.1%2c0%2c30.2%2c0.7%2c45%2c1.9 c8.3%2c11.9%2c16.4%2c24.6%2c24.2%2c38c7.6%2c13.1%2c14.5%2c26.4%2c20.8%2c39.8C504.7%2c309.8%2c497.8%2c323.2%2c490.3%2c336.3z M522.6%2c323.3 c5.4%2c13.4%2c10%2c26.8%2c13.8%2c39.8c-13.1%2c3.2-26.9%2c5.9-41.2%2c8c4.9-7.7%2c9.8-15.6%2c14.4-23.7C514.2%2c339.4%2c518.5%2c331.3%2c522.6%2c323.3z M421.2%2c430c-9.3-9.6-18.6-20.3-27.8-32c9%2c0.4%2c18.2%2c0.7%2c27.5%2c0.7c9.4%2c0%2c18.7-0.2%2c27.8-0.7C439.7%2c409.7%2c430.4%2c420.4%2c421.2%2c430z M346.8%2c371.1c-14.2-2.1-27.9-4.7-41-7.9c3.7-12.9%2c8.3-26.2%2c13.5-39.5c4.1%2c8%2c8.4%2c16%2c13.1%2c24C337.1%2c355.7%2c341.9%2c363.5%2c346.8%2c371.1z M420.7%2c163c9.3%2c9.6%2c18.6%2c20.3%2c27.8%2c32c-9-0.4-18.2-0.7-27.5-0.7c-9.4%2c0-18.7%2c0.2-27.8%2c0.7C402.2%2c183.3%2c411.5%2c172.6%2c420.7%2c163z M346.7%2c221.9c-4.9%2c7.7-9.8%2c15.6-14.4%2c23.7c-4.6%2c8-8.9%2c16-13%2c24c-5.4-13.4-10-26.8-13.8-39.8C318.6%2c226.7%2c332.4%2c224%2c346.7%2c221.9z M256.2%2c347.1c-35.4-15.1-58.3-34.9-58.3-50.6c0-15.7%2c22.9-35.6%2c58.3-50.6c8.6-3.7%2c18-7%2c27.7-10.1c5.7%2c19.6%2c13.2%2c40%2c22.5%2c60.9 c-9.2%2c20.8-16.6%2c41.1-22.2%2c60.6C274.3%2c354.2%2c264.9%2c350.8%2c256.2%2c347.1z M310%2c490c-13.6-7.8-19.5-37.5-14.9-75.7 c1.1-9.4%2c2.9-19.3%2c5.1-29.4c19.6%2c4.8%2c41%2c8.5%2c63.5%2c10.9c13.5%2c18.5%2c27.5%2c35.3%2c41.6%2c50c-32.6%2c30.3-63.2%2c46.9-84%2c46.9 C316.8%2c492.6%2c313%2c491.7%2c310%2c490z M547.2%2c413.8c4.7%2c38.2-1.1%2c67.9-14.6%2c75.8c-3%2c1.8-6.9%2c2.6-11.5%2c2.6c-20.7%2c0-51.4-16.5-84-46.6 c14-14.7%2c28-31.4%2c41.3-49.9c22.6-2.4%2c44-6.1%2c63.6-11C544.3%2c394.8%2c546.1%2c404.5%2c547.2%2c413.8z M585.7%2c347.1c-8.6%2c3.7-18%2c7-27.7%2c10.1 c-5.7-19.6-13.2-40-22.5-60.9c9.2-20.8%2c16.6-41.1%2c22.2-60.6c9.9%2c3.1%2c19.3%2c6.5%2c28.1%2c10.2c35.4%2c15.1%2c58.3%2c34.9%2c58.3%2c50.6 C644%2c312.2%2c621.1%2c332.1%2c585.7%2c347.1z'/> <polygon fill='%2361DAFB' points='320.8%2c78.4 320.8%2c78.4 320.8%2c78.4 '/> <circle fill='%2361DAFB' cx='420.9' cy='296.5' r='45.7'/> <polygon fill='%2361DAFB' points='520.5%2c78.1 520.5%2c78.1 520.5%2c78.1 '/> </g> </svg>`,
            metadata: {
              language: 'javascript'
            }
          },
          service: 'tako-test-frontend',
          route: 'tako-test-frontend',
        },
      },
      capabilities: [
        {
          module: 'web-app',
          props: {},
          extra: {}
        }
      ],
      subFolderName: 'frontend'
    },
  ]
} as AppDefinition;

  export default mockAppDefinition;
  