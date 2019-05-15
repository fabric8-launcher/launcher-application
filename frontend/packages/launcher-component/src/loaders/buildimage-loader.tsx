import React from 'react';
import _ from 'lodash';
import { AnalyzeResult, BuilderImage } from 'launcher-client';
import { useLauncherClient } from '../contexts/launcher-client-context';
import { DataLoader } from '../core/data-loader/data-loader';

export function BuildImageAnalyzerLoader(props: { gitUrl: string, children: (result: AnalyzeResult) => any }) {
  const client = useLauncherClient();
  const itemsLoader = () => client.importAnalyze(props.gitUrl);
  return (
    <DataLoader loader={itemsLoader}>
      {props.children}
    </DataLoader>
  );
}

interface BuildImageSuggestions {
  builderImages: BuilderImage[];
  suggestedBuilderImage: BuilderImage;
  getSuggestedEnvPairs: (image: string) => string[][];
}

interface BuildImageSuggestionsLoaderProps {
  gitUrl: string;
  children: (suggestions: BuildImageSuggestions) => any;
}

const convertToPairs = (object?: {[key: string]: string}): string[][] => {
  return _.toPairs(object);
};

export const convertToObject = (vars: string[][]) => {
  return _.fromPairs(vars.filter(p => p[0] !== ''));
};

const findBuilderImage = (result: AnalyzeResult, image?: string) => {
  const found = result.builderImages.find(i => i.id === (image || result.image));
  if (!found) {
    throw Error('invalid builder image');
  }
  return found;
};

export function BuildImageSuggestionsLoader(props: BuildImageSuggestionsLoaderProps) {
  const client = useLauncherClient();
  const itemsLoader = async () => {
    const result = await client.importAnalyze(props.gitUrl);
    const suggestedBuilderImage = findBuilderImage(result);
    const getSuggestedEnvPairs = (image: string) => {
      const imageMetadata = findBuilderImage(result, image).metadata;
      return convertToPairs(imageMetadata ? imageMetadata.suggestedEnv : undefined);
    };
    return { suggestedBuilderImage, getSuggestedEnvPairs, builderImages: result.builderImages };
  };
  return (
    <DataLoader loader={itemsLoader}>
      {props.children}
    </DataLoader>
  );
}
