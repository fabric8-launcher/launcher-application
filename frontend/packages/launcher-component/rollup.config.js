import peerDepsExternal from 'rollup-plugin-peer-deps-external';
import postcss from 'rollup-plugin-postcss';
import filesize from 'rollup-plugin-filesize';
import tslint from 'rollup-plugin-tslint';
import typescript from 'rollup-plugin-typescript2';

import pkg from './package.json';

const config = {
    input: 'src/index.ts',
    output: [
        {
            file: pkg.main,
            format: 'cjs',
        },
        {
            file: pkg.module,
            format: 'es',
        },
    ],
    external: [
        ...Object.keys(pkg.dependencies || {}),
        ...Object.keys(pkg.peerDependencies || {}),
    ],
    plugins: [
        peerDepsExternal(),
        postcss({
            modules: true
        }),
        tslint({
            include: './src/**/*.tsx?'
        }),
        typescript({
            clean: true,
            typescript: require('typescript'),
            verbosity: 0,
            useTsconfigDeclarationDir: true
        }),
        filesize(),
    ],
};

export default config;
